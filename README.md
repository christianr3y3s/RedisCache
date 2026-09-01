# Saga Orchestrator (Rust + Kafka) — config-driven

Este projeto é um **orquestrador genérico** (control-plane) para fluxos estilo **Saga**, dirigido por **eventos Kafka**.
A ideia é: **ninguém mexe no código**; fluxo, eventos, rotas e timeouts ficam em `orchestrator.properties`.

## O que já vem pronto
- Consumer Kafka async (rdkafka + tokio)
- Producer Kafka async
- Leitura de `orchestrator.properties` (parser simples, sem libs mágicas)
- State Store via **tópico compactado** (`_orchestrator.state`) com chave = `sagaType|sagaId`
- DLQ (`_orchestrator.dlq`) e Retry (`_orchestrator.retry`) configuráveis

## Pré-requisitos
- Docker (para Kafka local)
- Rust (rustup)

## Subir Kafka local
```bash
docker compose up -d
```

## Criar tópicos (inclui state compactado)
```bash
./scripts/create-topics.sh
```

## Rodar o orquestrador
```bash
cargo run
```

## Teste rápido: produzir evento
Use o console producer do Kafka (dentro do container):
```bash
docker exec -it kafka bash
kafka-console-producer --bootstrap-server localhost:9092 --topic orders.created
```
Cole um JSON (precisa ter `type` e `correlationId` por padrão):
```json
{"type":"OrderCreated","correlationId":"order-123","payload":{"x":1}}
```

Você verá no log do orquestrador:
- evento recebido
- comandos publicados (definidos no properties)
- estado atualizado no `_orchestrator.state`

## Onde mudar o comportamento
- `orchestrator.properties` (tudo)

## Observações
- Este template mantém o motor genérico. Você pode evoluir as regras para uma FSM (máquina de estados) declarativa usando o mesmo arquivo.

# Security and Identity

This repository documents **application-level identity decisions explicitly**.

Events and commands may carry a JSON Web Token (JWT) inside the payload in order to
express logical identity and authorization in a language-agnostic way.

Transport- and broker-level security (TLS, SASL, ACLs) are intentionally treated as
separate concerns and are expected to be enforced by the messaging infrastructure.