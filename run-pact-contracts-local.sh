#!/usr/bin/env bash

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")" && pwd)"
FLEET_POM="$ROOT_DIR/fleet-state-service/pom.xml"
SIM_POM="$ROOT_DIR/robot-simulator/pom.xml"
PACT_DIR="$ROOT_DIR/pacts"

mkdir -p "$PACT_DIR"

echo "[1/4] Generating pacts from fleet-state-service consumers"
mvn -f "$FLEET_POM" -Ppact-generate -Dpact.rootDir="$PACT_DIR" test

echo "[2/4] Generating pacts from robot-simulator consumers (if present)"
mvn -f "$SIM_POM" -Ppact-generate -Dpact.rootDir="$PACT_DIR" test

echo "[3/4] Verifying provider pacts in fleet-state-service"
mvn -f "$FLEET_POM" -Ppact-verify test

echo "[4/4] Verifying provider pacts in robot-simulator"
mvn -f "$SIM_POM" -Ppact-verify test

echo "Pact contract flow completed successfully."
