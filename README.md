# RoboFleet Control Plane MVP

This repository contains a multi-module MVP for a **RoboFleet Control Plane** idea.

## MVP Scope

- Simulate 3 robots emitting telemetry every second
- Publish telemetry events to Kafka (`robot.telemetry` topic)
- Consume events in a Spring Boot backend
- Persist latest robot state to SQLite
- Expose API and a tiny dashboard for fleet visibility

## Repository Structure

```text
.
├── robot-simulator/        # Python telemetry producer
├── fleet-state-service/    # Spring Boot Kafka consumer + REST API
├── fleet-dashboard-ui/     # Vanilla JS dashboard consuming /api/robots
└── docker-compose.yml      # Local Kafka infrastructure
```

## Prerequisites

- Docker / Docker Compose
- Python 3.10+
- Java 21+
- Maven 3.9+

## 1) Start Kafka

```bash
docker compose up -d
```

Kafka will be reachable at `localhost:9092`.

## 2) Run Fleet State Service

```bash
cd fleet-state-service
mvn spring-boot:run
```

Service starts at `http://localhost:8080`.

## 3) Run Robot Simulator

Open a second terminal:

```bash
cd robot-simulator
python3 -m venv .venv
source .venv/bin/activate
pip install -r requirements.txt
python robot_simulator.py
```

## Verify

- Dashboard (new JS module): `http://localhost:5173/?apiBase=http://localhost:8080`
- API all robots: `http://localhost:8080/api/robots`
- API one robot: `http://localhost:8080/api/robots/robot-1`

## One-command MVP launch

Start everything (Kafka + backend + simulator):

```bash
chmod +x scripts/mvp-up.sh scripts/mvp-down.sh
./scripts/mvp-up.sh
```

`mvp-up.sh` automatically chooses:

- Docker Kafka (if Docker is available), or
- Local no-Docker Kafka (downloads and runs Apache Kafka under `.local/`)

Stop everything:

```bash
./scripts/mvp-down.sh
```

Logs are written to:

- `.run/fleet-state-service.log`
- `.run/robot-simulator.log`
- `.run/fleet-dashboard-ui.log`
- `.run/kafka.log` (when running local no-Docker Kafka)

## Telemetry Event Shape

```json
{
  "robotId": "robot-1",
  "x": 12.34,
  "y": 56.78,
  "battery": 87.1,
  "status": "MOVING",
  "timestamp": "2026-05-19T16:40:03.123456Z"
}
```
