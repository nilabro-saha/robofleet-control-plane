# RoboFleet Control Plane MVP

This repository contains a multi-module MVP for a **RoboFleet Control Plane** idea.

The intent of this project is to demonstrate a clear separation of concerns that often appears in real robotics platforms:

- an **edge producer** (robots/simulators) that emits telemetry,
- a **control-plane state service** that materializes fleet state from a stream,
- and a **read-only operator UI** that visualizes current state.

## MVP Scope

- Simulate 3 robots emitting telemetry every second
- Publish telemetry events to Kafka (`robot.telemetry` topic)
- Consume events in a Spring Boot backend
- Persist latest robot state to SQLite
- Expose API and a tiny dashboard for fleet visibility

This MVP intentionally focuses on **current state materialization** rather than historical analytics. It is a “what is happening now?” system for operators.

## Repository Structure

```text
.
├── robot-simulator/        # Spring Boot robot actor telemetry producer
├── fleet-state-service/    # Spring Boot Kafka consumer + REST API
├── fleet-dashboard-ui/     # Vanilla JS dashboard consuming /api/robots
├── scripts/                # One-command orchestration helpers
└── docker-compose.yml      # Local Kafka infrastructure
```

For deeper intent and file-level guidance, see module docs:

- `robot-simulator/README.md`
- `fleet-state-service/README.md`
- `fleet-dashboard-ui/README.md`
- `scripts/README.md`

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

If `8080` is already occupied, the one-command launcher auto-selects `8081`.

## 3) Run Robot Simulator

Open a second terminal:

```bash
cd robot-simulator
mvn spring-boot:run
```

The simulator continuously emits synthetic state for robot actors and is intentionally configurable so you can quickly tune behavior (spawn count, map bounds, publish interval).

## Verify

- Dashboard (new JS module): `http://localhost:5173/?apiBase=http://localhost:8080`
- API all robots: `http://localhost:8080/api/robots`
- API sorted robots (pageable): `http://localhost:8080/api/robots?sort=timestamp,desc&size=3`
- API one robot: `http://localhost:8080/api/robots/robot-1`

If `mvp-up.sh` moved ports (for example to `8081`), use the dashboard URL printed by the script.

### Dashboard behaviors

- Click any table row to open a right-side robot details panel
- Click table headers to change backend-driven sorting
  - click once for ascending
  - click same header again for descending
- Sorting is handled by backend API/database query path (not by frontend array sorting)

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

These logs are operational artifacts only and are intentionally ignored by git.

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
