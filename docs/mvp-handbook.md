# RoboFleet MVP Handbook (Detailed Technical Guide)

This handbook preserves the detailed operational and technical context of the RoboFleet MVP.

If you want the quick portfolio-style overview, go to the root [`README.md`](../README.md).

---

## 1) Project intent

RoboFleet is a multi-module MVP for a robotics-style control plane architecture with clear separation between:

- **edge/runtime simulation** (robots producing telemetry),
- **control-plane state service** (materialized latest state),
- **operator view** (dashboard for current visibility).

The MVP emphasizes **current-state materialization** (“what is happening right now?”) rather than historical analytics.

---

## 2) MVP scope

- Simulate robot actors that emit telemetry on a fixed interval
- Publish telemetry/lifecycle events through Kafka
- Consume and materialize latest state in a Spring Boot backend + SQLite
- Expose query APIs for fleet and per-robot views
- Provide a lightweight dashboard for operator visibility
- Support contract verification across module boundaries using Pact

---

## 3) Repository structure

```text
.
├── robot-simulator/        # Spring Boot robot actor telemetry/lifecycle producer-runtime
├── fleet-state-service/    # Spring Boot Kafka consumer + REST API + state materialization
├── fleet-dashboard-ui/     # Vanilla JS dashboard consuming backend APIs
├── scripts/                # one-command local orchestration helpers
├── docs/contracts/         # versioned event/message contracts
├── docs/roadmap/           # phase-by-phase evolution plan
└── docker-compose.yml      # local Kafka infrastructure
```

Module-level details:

- [`robot-simulator/README.md`](../robot-simulator/README.md)
- [`fleet-state-service/README.md`](../fleet-state-service/README.md)
- [`fleet-dashboard-ui/README.md`](../fleet-dashboard-ui/README.md)
- [`scripts/README.md`](../scripts/README.md)

---

## 4) Local setup and run

### Prerequisites

- Docker / Docker Compose
- Python 3.10+
- Java 21+
- Maven 3.9+

### Start Kafka only

```bash
docker compose up -d
```

Kafka will be reachable at `localhost:9092`.

### Run backend service

```bash
cd fleet-state-service
mvn spring-boot:run
```

Default base URL: `http://localhost:8080`.

### Run simulator service

```bash
cd robot-simulator
mvn spring-boot:run
```

The simulator continuously emits synthetic state for robot actors and supports runtime configuration (spawn count, map bounds, publish interval).

### Verify manually

- Dashboard: `http://localhost:5173/?apiBase=http://localhost:8080`
- Fleet state list: `http://localhost:8080/api/robot-statuses`
- Fleet state sortable query example: `http://localhost:8080/api/robot-statuses?sort=timestamp,desc&size=3`
- Single robot latest state: `http://localhost:8080/api/robot-statuses/{id}`
- Robot identity/lifecycle list: `http://localhost:8080/api/robots`

If scripts pick a fallback port (for example `8081`), use the URL printed by script output.

---

## 5) One-command orchestration

Start everything (Kafka + backend + simulator + dashboard):

```bash
chmod +x scripts/mvp-up.sh scripts/mvp-down.sh
./scripts/mvp-up.sh
```

`mvp-up.sh` can run with:

- Docker Kafka (when Docker is available), or
- Local no-Docker Kafka fallback (under `.local/`).

Stop everything:

```bash
./scripts/mvp-down.sh
```

Runtime logs are written to `.run/`:

- `.run/fleet-state-service.log`
- `.run/robot-simulator.log`
- `.run/fleet-dashboard-ui.log`
- `.run/kafka.log` (local no-Docker Kafka path)

---

## 6) API surface (current)

### Fleet status projection APIs

- `GET /api/robot-statuses`
- `GET /api/robot-statuses/{id}`

`GET /api/robot-statuses` supports pageable/sort params:

- `sort=<field>,<direction>`
- `size=<number>`
- `page=<number>`

### Robot lifecycle/identity APIs

- `GET /api/robots`
- `GET /api/robots/{id}`
- `POST /api/robots`
- `DELETE /api/robots/{id}`

---

## 7) Dashboard behavior

- Table polling for fleet status via `/api/robot-statuses`
- Create robot action from UI (Add button)
- Row click opens right-side detail panel
- Header-click backend-driven sorting (not in-browser array sorting)
- Local-time timestamp display for operator readability

---

## 8) Event contracts and sample payloads

Official contract docs:

- [`docs/contracts/robot-telemetry-v1.md`](./contracts/robot-telemetry-v1.md)
- [`docs/contracts/robot-lifecycle-v1.md`](./contracts/robot-lifecycle-v1.md)
- [`docs/contracts/robot-task-command-v1-draft.md`](./contracts/robot-task-command-v1-draft.md)
- [`docs/contracts/robot-task-status-v1-draft.md`](./contracts/robot-task-status-v1-draft.md)

Sample telemetry event shape:

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

---

## 9) Contract testing (Pact)

This repository includes Pact-based contract flows for:

- API contract validation (`fleet-dashboard-ui` -> `fleet-state-service`)
- Event/message contracts in both directions:
  - `robot-simulator` -> `fleet-state-service`
  - `fleet-state-service` -> `robot-simulator`

Run full flow from repository root:

```bash
mvn -f pom.xml verify
```

Root orchestration order:

1. Fleet consumer pact generation
2. Simulator consumer pact generation
3. Fleet provider verification
4. Simulator provider verification

Pacts are written to top-level `pacts/` (gitignored).

Run module lifecycle only (skip root contract orchestration):

```bash
mvn -f pom.xml -DskipContractTests=true verify
```

---

## 10) Evolution plan (control plane -> ROS2/Gazebo)

Target direction:

- task creation from dashboard,
- task assignment and lifecycle tracking in control plane,
- robot execution via ROS2 runtime,
- simulation validation in Gazebo,
- near-real-time progress feedback back to dashboard.

Full phase plans:

- [`docs/roadmap/phase-01-simulator-runtime-rehydration.md`](./roadmap/phase-01-simulator-runtime-rehydration.md)
- [`docs/roadmap/phase-02-foundation-alignment.md`](./roadmap/phase-02-foundation-alignment.md)
- [`docs/roadmap/phase-03-task-domain-control-plane.md`](./roadmap/phase-03-task-domain-control-plane.md)
- [`docs/roadmap/phase-04-dashboard-task-ux.md`](./roadmap/phase-04-dashboard-task-ux.md)
- [`docs/roadmap/phase-05-ros2-bridge-layer.md`](./roadmap/phase-05-ros2-bridge-layer.md)
- [`docs/roadmap/phase-06-gazebo-simulation-execution.md`](./roadmap/phase-06-gazebo-simulation-execution.md)
- [`docs/roadmap/phase-07-reliability-safety-and-scale.md`](./roadmap/phase-07-reliability-safety-and-scale.md)

Roadmap index: [`docs/roadmap/README.md`](./roadmap/README.md)

---

## 11) Suggested work breakdown by module

- `fleet-state-service`
  - task domain, persistence, command/status messaging
- `robot-simulator`
  - bridge-friendly runtime behavior or dedicated bridge module
- `fleet-dashboard-ui`
  - task creation/timeline/robot-task association UX
- `scripts`
  - optional ROS2/Gazebo launch profiles

---

## 12) Tooling note: CAD vs simulation runtime

- SolidWorks is typically a CAD/model-authoring source.
- Gazebo is the runtime simulation target in this roadmap.
- Typical chain: CAD assets -> URDF/meshes -> ROS2 + Gazebo execution.

---

## 13) Definition of done (pick-and-place from dashboard)

Capability is complete when all are true:

- Task created from dashboard with pickup/drop coordinates
- Task assigned to a robot and status is visible live
- Robot motion observable in Gazebo during execution
- Completion/failure status propagates back to control plane and UI
- Task timeline is queryable through API
