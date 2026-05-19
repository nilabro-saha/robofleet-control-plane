#!/usr/bin/env bash

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
RUN_DIR="$ROOT_DIR/.run"
KAFKA_PID_FILE="$RUN_DIR/kafka.pid"

if [[ ! -f "$KAFKA_PID_FILE" ]]; then
  echo "[kafka-down] Kafka not running (no pid file)."
  exit 0
fi

pid="$(cat "$KAFKA_PID_FILE")"

if kill -0 "$pid" >/dev/null 2>&1; then
  echo "[kafka-down] Stopping Kafka (PID $pid)..."
  kill "$pid" >/dev/null 2>&1 || true
else
  echo "[kafka-down] Kafka process not found (PID $pid)."
fi

rm -f "$KAFKA_PID_FILE"
echo "[kafka-down] Done."
