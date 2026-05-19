#!/usr/bin/env bash

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
RUN_DIR="$ROOT_DIR/.run"

SERVICE_PID_FILE="$RUN_DIR/fleet-state-service.pid"
SIM_PID_FILE="$RUN_DIR/robot-simulator.pid"
SERVICE_PORT_FILE="$RUN_DIR/fleet-state-service.port"
UI_PID_FILE="$RUN_DIR/fleet-dashboard-ui.pid"
UI_PORT_FILE="$RUN_DIR/fleet-dashboard-ui.port"

stop_pid_file() {
  local name="$1"
  local pid_file="$2"

  if [[ ! -f "$pid_file" ]]; then
    echo "[mvp-down] $name not running (no pid file)."
    return
  fi

  local pid
  pid="$(cat "$pid_file")"

  if kill -0 "$pid" >/dev/null 2>&1; then
    echo "[mvp-down] Stopping $name (PID $pid)..."
    kill "$pid" >/dev/null 2>&1 || true
  else
    echo "[mvp-down] $name process not found (PID $pid)."
  fi

  rm -f "$pid_file"
}

stop_pid_file "fleet-state-service" "$SERVICE_PID_FILE"
stop_pid_file "robot-simulator" "$SIM_PID_FILE"
stop_pid_file "fleet-dashboard-ui" "$UI_PID_FILE"
rm -f "$SERVICE_PORT_FILE"
rm -f "$UI_PORT_FILE"

if command -v docker >/dev/null 2>&1; then
  echo "[mvp-down] Stopping Kafka (Redpanda)..."
  docker compose -f "$ROOT_DIR/docker-compose.yml" down >/dev/null 2>&1 || true
else
  "$ROOT_DIR/scripts/kafka-local-down.sh" || true
fi

echo "[mvp-down] Done."
