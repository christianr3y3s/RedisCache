# Lightweight Rust Saga Event Orchestrator

[![Rust](https://img.shields.io/badge/Rust-1.75%2B-orange.svg)](https://www.rust-lang.org/)
[![Tokio](https://img.shields.io/badge/Async-Tokio-blue.svg)](https://tokio.rs/)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)

A minimal, ultra-low-overhead Saga Pattern orchestrator written in Rust. Designed for event-driven architectures requiring strict transaction consistency, fast execution, and zero unnecessary dependencies.

---

## 🎯 Purpose & Key Features

This orchestrator manages distributed transactions across microservices using the **Saga Execution Coordinator (SEC)** pattern (Orchestration-based Saga).

- **Minimal Footprint:** Zero heavy runtime frameworks—just lightweight async Rust (`tokio`).
- **Resilient Compensation:** Built-in forward execution and automatic backward compensation triggers on failure.
- **Event-Driven & Decoupled:** Consumes and routes domain events with minimal memory overhead.
- **Predictable Latency:** No GC pauses, ensuring deterministic execution times for critical workflows.

---

## 🏛️ Architecture Overview

```text
       [ Incoming Event ]
               │
               ▼
   ┌───────────────────────┐
   │    Saga Orchestrator  │  <--- State Machine (Rust)
   └───────────┬───────────┘
               │
     ┌─────────┴─────────┐
     ▼                   ▼
[ Step 1: Execute ]  [ Failure Detection ]
     │                   │
     ▼                   ▼
[ Step 2: Next ]     [ Rollback / Compensate ]
