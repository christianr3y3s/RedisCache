#!/usr/bin/env bash
set -euo pipefail

KAFKA_CONTAINER=${KAFKA_CONTAINER:-kafka}
BOOTSTRAP=${BOOTSTRAP:-localhost:9092}

function kt() {
  docker exec -i "$KAFKA_CONTAINER" bash -lc "$*"
}

echo "Creating topics..."

# input topics
kt "kafka-topics --bootstrap-server $BOOTSTRAP --create --if-not-exists --topic orders.created --partitions 1 --replication-factor 1"
kt "kafka-topics --bootstrap-server $BOOTSTRAP --create --if-not-exists --topic payments.reserved --partitions 1 --replication-factor 1"
kt "kafka-topics --bootstrap-server $BOOTSTRAP --create --if-not-exists --topic inventory.confirmed --partitions 1 --replication-factor 1"
kt "kafka-topics --bootstrap-server $BOOTSTRAP --create --if-not-exists --topic inventory.failed --partitions 1 --replication-factor 1"

# commands
kt "kafka-topics --bootstrap-server $BOOTSTRAP --create --if-not-exists --topic payments.reserve --partitions 1 --replication-factor 1"
kt "kafka-topics --bootstrap-server $BOOTSTRAP --create --if-not-exists --topic inventory.confirm --partitions 1 --replication-factor 1"
kt "kafka-topics --bootstrap-server $BOOTSTRAP --create --if-not-exists --topic payments.rollback --partitions 1 --replication-factor 1"
kt "kafka-topics --bootstrap-server $BOOTSTRAP --create --if-not-exists --topic saga.completed --partitions 1 --replication-factor 1"
kt "kafka-topics --bootstrap-server $BOOTSTRAP --create --if-not-exists --topic saga.failed --partitions 1 --replication-factor 1"

# infra
kt "kafka-topics --bootstrap-server $BOOTSTRAP --create --if-not-exists --topic _orchestrator.dlq --partitions 1 --replication-factor 1"
kt "kafka-topics --bootstrap-server $BOOTSTRAP --create --if-not-exists --topic _orchestrator.retry --partitions 1 --replication-factor 1"

# state store (compacted)
kt "kafka-topics --bootstrap-server $BOOTSTRAP --create --if-not-exists --topic _orchestrator.state --partitions 1 --replication-factor 1 --config cleanup.policy=compact"

echo "Done."
