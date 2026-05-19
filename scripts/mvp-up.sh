#!/usr/bin/env bash

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
RUN_DIR="$ROOT_DIR/.run"
mkdir -p "$RUN_DIR"

SERVICE_PID_FILE="$RUN_DIR/fleet-state-service.pid"
SIM_PID_FILE="$RUN_DIR/robot-simulator.pid"
SERVICE_PORT_FILE="$RUN_DIR/fleet-state-service.port"

echo "[mvp-up] Root: $ROOT_DIR"

is_pid_alive() {
  local pid_file="$1"
  [[ -f "$pid_file" ]] || return 1
  local pid
  pid="$(cat "$pid_file")"
  kill -0 "$pid" >/dev/null 2>&1
}

if [[ -f "$SERVICE_PID_FILE" ]] && ! is_pid_alive "$SERVICE_PID_FILE"; then
  rm -f "$SERVICE_PID_FILE" "$SERVICE_PORT_FILE"
fi

if [[ -f "$SIM_PID_FILE" ]] && ! is_pid_alive "$SIM_PID_FILE"; then
  rm -f "$SIM_PID_FILE"
fi

if [[ -f "$SERVICE_PID_FILE" ]] || [[ -f "$SIM_PID_FILE" ]]; then
  echo "[mvp-up] Existing live PID files found in .run/. Run './scripts/mvp-down.sh' first."
  exit 1
fi

if ! command -v mvn >/dev/null 2>&1; then
  echo "[mvp-up] Maven is required but not found in PATH."
  exit 1
fi

if command -v python3.10 >/dev/null 2>&1; then
  PYTHON_BIN="python3.10"
elif command -v python3 >/dev/null 2>&1; then
  PYTHON_BIN="python3"
else
  echo "[mvp-up] Python 3 is required but not found in PATH."
  exit 1
fi

kafka_reachable() {
  if command -v nc >/dev/null 2>&1; then
    nc -z localhost 9092 >/dev/null 2>&1
  else
    (echo > /dev/tcp/127.0.0.1/9092) >/dev/null 2>&1
  fi
}

port_in_use() {
  local port="$1"
  if command -v nc >/dev/null 2>&1; then
    nc -z localhost "$port" >/dev/null 2>&1
  else
    (echo > /dev/tcp/127.0.0.1/"$port") >/dev/null 2>&1
  fi
}

if kafka_reachable; then
  echo "[mvp-up] Existing Kafka detected on localhost:9092. Reusing host broker."
elif command -v docker >/dev/null 2>&1; then
  echo "[mvp-up] Starting Kafka via Docker Compose..."
  docker compose -f "$ROOT_DIR/docker-compose.yml" up -d
else
  echo "[mvp-up] Docker not found. Starting local Kafka without Docker..."
  "$ROOT_DIR/scripts/kafka-local-up.sh"
fi

echo "[mvp-up] Preparing simulator virtualenv..."
if [[ ! -d "$ROOT_DIR/robot-simulator/.venv" ]]; then
  "$PYTHON_BIN" -m venv "$ROOT_DIR/robot-simulator/.venv"
fi

"$ROOT_DIR/robot-simulator/.venv/bin/python" -m pip install -r "$ROOT_DIR/robot-simulator/requirements.txt" >/dev/null

echo "[mvp-up] Starting fleet-state-service..."
SERVICE_PORT=8080
if port_in_use "$SERVICE_PORT"; then
  SERVICE_PORT=8081
  echo "[mvp-up] Port 8080 is in use. Starting fleet-state-service on port ${SERVICE_PORT}."
fi

echo "$SERVICE_PORT" > "$SERVICE_PORT_FILE"

nohup mvn -q -DskipTests spring-boot:run \
  --file "$ROOT_DIR/fleet-state-service/pom.xml" \
  -Dspring-boot.run.jvmArguments="-Dserver.port=${SERVICE_PORT}" \
  > "$RUN_DIR/fleet-state-service.log" 2>&1 &
echo $! > "$SERVICE_PID_FILE"

echo "[mvp-up] Starting robot-simulator..."
nohup "$ROOT_DIR/robot-simulator/.venv/bin/python" "$ROOT_DIR/robot-simulator/src/robot_simulator.py" \
  > "$RUN_DIR/robot-simulator.log" 2>&1 &
echo $! > "$SIM_PID_FILE"

echo "[mvp-up] Started successfully"
echo "  Fleet service PID: $(cat "$SERVICE_PID_FILE")"
echo "  Simulator PID:     $(cat "$SIM_PID_FILE")"
echo "  Logs:"
echo "    - $RUN_DIR/fleet-state-service.log"
echo "    - $RUN_DIR/robot-simulator.log"
echo "[mvp-up] Dashboard: http://localhost:${SERVICE_PORT}"
