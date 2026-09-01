use crate::{config::OrchestratorConfig, state_store::{LastEvent, SagaState, StateCache}};
use anyhow::Result;
use rdkafka::{
    config::ClientConfig,
    consumer::{Consumer, StreamConsumer},
    message::Message,
    producer::{FutureProducer, FutureRecord},
};
use serde_json::Value;
use std::time::{SystemTime, UNIX_EPOCH, Duration};
use tracing::{info, warn};

pub struct Orchestrator {
    cfg: OrchestratorConfig,
    consumer: StreamConsumer,
    producer: FutureProducer,
    pub state: StateCache,
}

impl Orchestrator {
    pub fn new(cfg: OrchestratorConfig) -> Result<Self> {
        let consumer: StreamConsumer = ClientConfig::new()
            .set("bootstrap.servers", &cfg.kafka.bootstrap_servers)
            .set("group.id", &cfg.kafka.group_id)
            .set("auto.offset.reset", &cfg.kafka.auto_offset_reset)
            .set("enable.auto.commit", if cfg.kafka.enable_auto_commit {"true"} else {"false"})
            .create()?;

        let producer: FutureProducer = ClientConfig::new()
            .set("bootstrap.servers", &cfg.kafka.bootstrap_servers)
            .create()?;

        Ok(Self { cfg, consumer, producer, state: StateCache::new() })
    }

    pub fn subscribe(&self) -> Result<()> {
        let topics: Vec<&str> = self.cfg.event_in.iter().map(|e| e.topic.as_str()).collect();
        self.consumer.subscribe(&topics)?;
        Ok(())
    }

    pub async fn run(mut self) -> Result<()> {
        info!("Orchestrator started");
        loop {
            match self.consumer.recv().await {
                Ok(msg) => {
                    if let Err(e) = self.handle_message(&msg).await {
                        warn!(error=%e, "message handling failed");
                    }
                }
                Err(e) => warn!("Kafka recv error: {}", e),
            }
        }
    }

    async fn handle_message(&mut self, msg: &rdkafka::message::BorrowedMessage<'_>) -> Result<()> {
        let topic = msg.topic().to_string();
        let payload = msg.payload_view::<str>().unwrap_or(Ok(""))?;

        // Parse JSON
        let json: Value = match serde_json::from_str(payload) {
            Ok(v) => v,
            Err(e) => {
                self.on_error("parse", &topic, payload, &e.to_string()).await?;
                return Ok(());
            }
        };

        // Determine event type
        let event_type = self.extract_event_type(&topic, &json).ok_or_else(|| anyhow::anyhow!("missing event type"))?;

        // Determine correlation id
        let saga_id = self.extract_correlation(&json).ok_or_else(|| anyhow::anyhow!("missing correlation"))?;
        let saga_type = "DefaultSaga".to_string(); // propositalmente genérico

        info!(%event_type, %saga_id, %topic, partition=msg.partition(), offset=msg.offset(), "event received");

        // Emit commands defined by config (v1 routing)
        if let Some(cmds) = self.cfg.routes.get(&event_type) {
            for cmd in cmds {
                self.emit_command(cmd, &saga_id, &json).await?;
            }
        } else {
            match self.cfg.error_policy.on_unknown_event.as_str() {
                "IGNORE" => {}
                _ => warn!(%event_type, "unknown event type (no route)")
            }
        }

        // Update and publish state
        let now_ms = now_ms();
        let prev = self.state.get(&saga_type, &saga_id).await;
        let next_version = prev.map(|s| s.version + 1).unwrap_or(1);

        let state = SagaState {
            saga_type: saga_type.clone(),
            saga_id: saga_id.clone(),
            status: "InProgress".into(),
            current: event_type.clone(),
            version: next_version,
            updated_at_ms: now_ms,
            last_event: LastEvent {
                event_type: event_type.clone(),
                topic: topic.clone(),
                partition: msg.partition(),
                offset: msg.offset(),
            },
            meta: serde_json::json!({"lastPayloadType": event_type}),
        };

        self.state.upsert(state.clone()).await;
        self.publish_state(&state).await?;

        Ok(())
    }

    fn extract_event_type(&self, topic: &str, json: &Value) -> Option<String> {
        match self.cfg.conventions.event_type_source.as_str() {
            "topic" => Some(topic.to_string()),
            _ => extract_dot_path(json, &self.cfg.conventions.event_type_path).and_then(|v| v.as_str().map(|s| s.to_string())),
        }
    }

    fn extract_correlation(&self, json: &Value) -> Option<String> {
        match self.cfg.conventions.correlation_source.as_str() {
            "payload" => extract_dot_path(json, &self.cfg.conventions.correlation_path).and_then(|v| v.as_str().map(|s| s.to_string())),
            _ => None,
        }
    }

    async fn emit_command(&self, cmd: &str, saga_id: &str, original: &Value) -> Result<()> {
        let topic = self.cfg.command_out.get(cmd)
            .ok_or_else(|| anyhow::anyhow!("command not configured: {cmd}"))?
            .to_string();

        // command envelope genérico
        let payload = serde_json::json!({
            "type": cmd,
            "correlationId": saga_id,
            "ts": now_ms(),
            "data": original
        }).to_string();

        let record = FutureRecord::to(&topic)
            .key(saga_id)
            .payload(&payload);

        let _ = self.producer.send(record, Duration::from_secs(5)).await
            .map_err(|(e, _)| anyhow::anyhow!("publish failed: {e}"))?;

        info!(%cmd, %topic, %saga_id, "command published");
        Ok(())
    }

    async fn publish_state(&self, state: &SagaState) -> Result<()> {
        let topic = &self.cfg.kafka.state_topic;
        let key = state.key();
        let value = serde_json::to_string(state)?;

        let record = FutureRecord::to(topic)
            .key(&key)
            .payload(&value);

        let _ = self.producer.send(record, Duration::from_secs(5)).await
            .map_err(|(e, _)| anyhow::anyhow!("state publish failed: {e}"))?;

        info!(%key, "state upserted");
        Ok(())
    }

    async fn on_error(&self, kind: &str, topic: &str, raw: &str, detail: &str) -> Result<()> {
        let policy = match kind {
            "parse" => self.cfg.error_policy.on_parse.as_str(),
            "missing_correlation" => self.cfg.error_policy.on_missing_correlation.as_str(),
            _ => "DLQ",
        };

        match policy {
            "DLQ" => {
                let payload = serde_json::json!({
                    "error": kind,
                    "detail": detail,
                    "topic": topic,
                    "raw": raw,
                    "ts": now_ms()
                }).to_string();
                let record = FutureRecord::to(&self.cfg.kafka.dlq_topic).payload(&payload);
                let _ = self.producer.send(record, Duration::from_secs(5)).await;
                warn!(%kind, %topic, "sent to DLQ");
            }
            _ => {
                warn!(%kind, %topic, %policy, "error policy applied (no action)");
            }
        }

        Ok(())
    }
}

fn now_ms() -> u64 {
    SystemTime::now().duration_since(UNIX_EPOCH).unwrap().as_millis() as u64
}

/// Suporte mínimo a "$.a.b.c" (não é JSONPath completo). Se quiser, dá pra trocar por lib depois.
fn extract_dot_path<'a>(json: &'a Value, path: &str) -> Option<&'a Value> {
    let p = path.trim();
    let p = p.strip_prefix("$.").unwrap_or(p);
    if p.is_empty() { return Some(json); }
    let mut cur = json;
    for part in p.split('.') {
        cur = cur.get(part)?;
    }
    Some(cur)
}
