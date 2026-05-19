#!/usr/bin/env bash

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
RUN_DIR="$ROOT_DIR/.run"
LOCAL_DIR="$ROOT_DIR/.local"
mkdir -p "$RUN_DIR" "$LOCAL_DIR"

KAFKA_VERSION="${KAFKA_VERSION:-3.8.0}"
SCALA_VERSION="${SCALA_VERSION:-2.13}"
KAFKA_DIST_DIR="$LOCAL_DIR/kafka"
KAFKA_FOLDER="kafka_${SCALA_VERSION}-${KAFKA_VERSION}"
KAFKA_HOME="$KAFKA_DIST_DIR/$KAFKA_FOLDER"
KAFKA_TGZ="$KAFKA_DIST_DIR/${KAFKA_FOLDER}.tgz"

KAFKA_PID_FILE="$RUN_DIR/kafka.pid"
KAFKA_CLUSTER_ID_FILE="$RUN_DIR/kafka-cluster.id"
KAFKA_CFG_FILE="$RUN_DIR/kafka-server.properties"
KAFKA_LOG_FILE="$RUN_DIR/kafka.log"
KAFKA_LOG_DIR="$RUN_DIR/kraft-combined-logs"

if [[ -f "$KAFKA_PID_FILE" ]]; then
  old_pid="$(cat "$KAFKA_PID_FILE")"
  if kill -0 "$old_pid" >/dev/null 2>&1; then
    echo "[kafka-up] Kafka already running (PID $old_pid)."
    exit 0
  else
    rm -f "$KAFKA_PID_FILE"
  fi
fi

if ! command -v curl >/dev/null 2>&1; then
  echo "[kafka-up] curl is required but not found in PATH."
  exit 1
fi

if ! command -v tar >/dev/null 2>&1; then
  echo "[kafka-up] tar is required but not found in PATH."
  exit 1
fi

mkdir -p "$KAFKA_DIST_DIR"

if [[ ! -d "$KAFKA_HOME" ]]; then
  if [[ ! -f "$KAFKA_TGZ" ]]; then
    KAFKA_URL="https://archive.apache.org/dist/kafka/${KAFKA_VERSION}/${KAFKA_FOLDER}.tgz"
    echo "[kafka-up] Downloading Kafka from: $KAFKA_URL"
    curl -fL "$KAFKA_URL" -o "$KAFKA_TGZ"
  fi

  echo "[kafka-up] Extracting Kafka..."
  tar -xzf "$KAFKA_TGZ" -C "$KAFKA_DIST_DIR"
fi

if [[ ! -x "$KAFKA_HOME/bin/kafka-server-start.sh" ]]; then
  echo "[kafka-up] Kafka binaries not found under $KAFKA_HOME"
  exit 1
fi

cp "$KAFKA_HOME/config/kraft/server.properties" "$KAFKA_CFG_FILE"

# Override critical local settings for predictable single-node development.
{
  echo ""
  echo "listeners=PLAINTEXT://0.0.0.0:9092,CONTROLLER://0.0.0.0:9093"
  echo "advertised.listeners=PLAINTEXT://localhost:9092"
  echo "log.dirs=$KAFKA_LOG_DIR"
} >> "$KAFKA_CFG_FILE"

if [[ ! -f "$KAFKA_CLUSTER_ID_FILE" ]]; then
  "$KAFKA_HOME/bin/kafka-storage.sh" random-uuid > "$KAFKA_CLUSTER_ID_FILE"
fi

cluster_id="$(cat "$KAFKA_CLUSTER_ID_FILE")"

if [[ ! -d "$KAFKA_LOG_DIR" ]] || [[ -z "$(ls -A "$KAFKA_LOG_DIR" 2>/dev/null || true)" ]]; then
  echo "[kafka-up] Formatting KRaft storage..."
  "$KAFKA_HOME/bin/kafka-storage.sh" format \
    --standalone \
    --config "$KAFKA_CFG_FILE" \
    --cluster-id "$cluster_id"
fi

echo "[kafka-up] Starting Kafka..."
nohup "$KAFKA_HOME/bin/kafka-server-start.sh" "$KAFKA_CFG_FILE" > "$KAFKA_LOG_FILE" 2>&1 &
echo $! > "$KAFKA_PID_FILE"

echo "[kafka-up] Kafka started (PID $(cat "$KAFKA_PID_FILE")) on localhost:9092"
