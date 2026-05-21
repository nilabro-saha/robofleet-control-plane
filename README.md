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

## Contract testing (Pact)

This repository includes local Pact-based contract testing for:

- API contracts (`fleet-dashboard-ui` -> `fleet-state-service`)
- Event/message contracts in both directions:
  - `robot-simulator` -> `fleet-state-service`
  - `fleet-state-service` -> `robot-simulator`

### Run full contract flow

From repository root:

```bash
bash ./run-pact-contracts-local.sh
```

This script runs:

1. Consumer pact generation in `fleet-state-service`
2. Consumer pact generation in `robot-simulator`
3. Provider verification in `fleet-state-service`
4. Provider verification in `robot-simulator`

Pact files are stored in the shared top-level `pacts/` folder.

For module-level details and class mapping, see:

- `fleet-state-service/README.md`
- `robot-simulator/README.md`

## Evolution Plan: Control Plane + Kafka + ROS2 + Gazebo

This section captures the intended path from the current MVP into a task-driven
robot operations platform where an operator can assign jobs like:

"Pick object at point A, then drop at point B"

and observe live execution progress in simulation.

### Vision (Target Capability)

- Operator creates task from dashboard (pickup + drop-off + optional priority)
- Control plane assigns task to a robot and tracks lifecycle end-to-end
- Robot-side runtime executes task using ROS2 stack
- Gazebo provides visual simulation of navigation/manipulation
- Task and robot state stream back into dashboard in near real time

### Target Architecture (High Level)

```text
Dashboard UI
  -> Task API (Fleet State/Task Service)
  -> Kafka topics (task.command, task.status, robot.telemetry, robot.lifecycle)
  -> ROS2-Kafka Bridge
  -> ROS2 robot runtime (nav + manipulation)
  -> Gazebo simulation world
  -> status/progress back via ROS2 -> Kafka -> Control Plane -> UI
```

### Key Design Principles

- Keep this repository as the **control plane source of truth**
- Use **Kafka** for decoupled command/status/event choreography
- Use **ROS2** as robot execution middleware (topics/services/actions)
- Use **Gazebo** for simulation and visual verification
- Preserve clear separation: planning/orchestration vs physical execution

## Phased Execution Roadmap

### Phase 1 — Simulator Runtime Rehydration (Do First)

Objective: ensure robots that already exist in control-plane persistence are
re-created in simulator runtime after service restarts, so they do not appear
as static/stale rows in dashboard.

Deliverables:

- Add startup rehydration flow in simulator:
  - fetch existing robots from control-plane API on boot
  - re-register only eligible robots (for example `ACTIVE`)
  - avoid duplicates if robot already exists in runtime registry
- Publish lifecycle/telemetry after re-registration so dashboard resumes live updates
- Add safeguards for partial failures (retry with backoff, dead-letter logging)
- Add tests for restart scenarios (service stop/start with pre-existing robots)

Exit criteria:

- After restarting services, previously created active robots resume movement
- Dashboard no longer shows persisted robots as permanently static due to missing runtime actors
- Rehydration is idempotent (no duplicate runtime actors)

### Phase 2 — Foundation Alignment (Current -> Next)

Objective: prepare current MVP for task orchestration without breaking existing behavior.

Deliverables:

- Document event contracts and versioning strategy
- Add correlation-id / causation-id conventions to messages
- Introduce explicit lifecycle state enums for robots/tasks
- Confirm API naming consistency (`/api/robot-statuses` vs `/api/robots` usage)

Exit criteria:

- Contract docs committed
- Compatibility tests for existing telemetry/lifecycle flow

### Phase 3 — Task Domain in Control Plane

Objective: support task creation, assignment, and status tracking.

Deliverables:

- Add task data model (example fields):
  - `taskId`, `taskType`, `pickupPose`, `dropPose`, `robotId`, `priority`, `status`
  - `createdAt`, `startedAt`, `completedAt`, `failureReason`
- Add DB persistence for tasks
- Add REST APIs:
  - `POST /api/tasks`
  - `GET /api/tasks`
  - `GET /api/tasks/{id}`
  - `POST /api/tasks/{id}/cancel` (optional)
- Add task command publisher to Kafka (`robot.task.command`)
- Add task status consumer (`robot.task.status`)

Exit criteria:

- Operator can create task via API
- Task appears in persisted state and transitions through at least:
  - `QUEUED -> ASSIGNED -> RUNNING -> COMPLETED/FAILED`

### Phase 4 — Dashboard Task UX

Objective: make task planning and progress visible to operators.

Deliverables:

- Add task creation UI (pickup/drop inputs)
- Add task list panel with filters and status colors
- Add task details panel (timeline, assigned robot, failure reason)
- Add robot-task linkage view (current active task per robot)

Exit criteria:

- End-to-end operator flow works from browser only
- Dashboard reflects status changes within polling interval

### Phase 5 — ROS2 Bridge Layer

Objective: connect control plane events with ROS2 robot execution runtime.

Deliverables:

- New ROS2 bridge service/package:
  - Kafka -> ROS2 (task command translation)
  - ROS2 -> Kafka (task progress + robot telemetry)
- Topic/action mapping (example):
  - Kafka `robot.task.command` -> ROS2 action goal
  - ROS2 feedback/result -> Kafka `robot.task.status`
- Robot identity + frame mapping conventions:
  - `robotId` <-> ROS2 namespace
  - map/world/base frame alignment

Exit criteria:

- A created control-plane task is consumed in ROS2 runtime
- Progress updates return to control plane and appear in UI

### Phase 6 — Gazebo Simulation Execution

Objective: visualize and validate actual robot movement and task completion.

Deliverables:

- Gazebo world + robot model setup (URDF/SDF)
- Navigation stack integration (path to pickup/drop)
- Basic manipulation workflow (or mock grasp/place state transitions initially)
- Sim-time synchronization and deterministic test scenarios

Exit criteria:

- Operator can watch robot move in Gazebo for assigned task
- Task closes as completed only after simulation result confirmation

### Phase 7 — Reliability, Safety, and Scale

Objective: make the system robust enough for multi-robot and failure scenarios.

Deliverables:

- Retry/dead-letter strategy for command/status topics
- Idempotent command handling and duplicate suppression
- Task timeout + stuck detection + manual intervention APIs
- Structured observability (metrics, tracing, audit trail)
- Role-based API access and secure broker transport

Exit criteria:

- Stable operation with multiple concurrent robots/tasks
- Clear operational runbook for common faults

## Event Contract Blueprint (Recommended)

### Kafka Topics

- `robot.telemetry` (existing)
- `robot.lifecycle` (existing)
- `robot.task.command` (new)
- `robot.task.status` (new)
- `robot.alerts` (optional)

### `robot.task.command` (example)

```json
{
  "eventId": "uuid",
  "correlationId": "uuid",
  "taskId": "task-123",
  "robotId": "robot-7",
  "taskType": "PICK_AND_PLACE",
  "pickup": { "x": 1.2, "y": 3.4, "z": 0.0, "frame": "map" },
  "dropoff": { "x": 8.9, "y": 2.1, "z": 0.0, "frame": "map" },
  "priority": "NORMAL",
  "createdAt": "2026-05-21T10:10:10Z"
}
```

### `robot.task.status` (example)

```json
{
  "eventId": "uuid",
  "correlationId": "uuid",
  "taskId": "task-123",
  "robotId": "robot-7",
  "status": "NAVIGATING_TO_PICKUP",
  "progress": 42,
  "message": "Approaching pickup waypoint",
  "timestamp": "2026-05-21T10:11:00Z"
}
```

## Suggested Repo-Level Work Breakdown

- `fleet-state-service`
  - add task domain, APIs, persistence, command/status messaging
- `robot-simulator`
  - evolve into bridge-friendly execution adapter (or add dedicated bridge module)
- `fleet-dashboard-ui`
  - add task creation + task timeline + robot-task association UI
- `scripts`
  - optional helpers for ROS2/Gazebo launch profiles

## Tooling Notes (SolidWorks vs Gazebo)

- SolidWorks is best treated as CAD/model authoring source.
- Recommended simulation runtime for this plan: Gazebo (with ROS2).
- Typical pipeline:
  - design in SolidWorks -> export meshes/URDF assets -> run behavior in Gazebo.

## Definition of Done for “Pick and Place from Dashboard”

The capability is considered complete when all are true:

- Task can be created from dashboard with pickup/drop coordinates
- Task is assigned to a specific robot and status is visible live
- Robot motion is observable in Gazebo during task execution
- Completion/failure status returns to control plane and updates UI
- Full task timeline is queryable by API
