mod config;
mod state_store;
mod orchestrator;

use crate::config::load_config;
use crate::orchestrator::Orchestrator;

#[tokio::main]
async fn main() -> anyhow::Result<()> {
    tracing_subscriber::fmt()
        .with_env_filter("info")
        .init();

    let config_path = std::env::var("ORCH_CONFIG").unwrap_or_else(|_| "orchestrator.properties".into());
    let cfg = load_config(&config_path)?;

    let orch = Orchestrator::new(cfg)?;
    orch.subscribe()?;
    orch.run().await?;

    Ok(())
}
