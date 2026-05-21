# Phase 5 — ROS2 Bridge Layer

## Objective

Introduce a bridge service that translates control-plane Kafka task messages to ROS2 execution primitives and returns ROS2 feedback/results back to Kafka.

## Scope

- Kafka -> ROS2 command translation
- ROS2 -> Kafka status/telemetry publishing
- Identity and frame mapping conventions

## Architecture intent

- Control-plane remains orchestration source of truth.
- ROS2 runtime remains execution source of truth for in-flight robot behavior.
- Bridge is stateless or minimally stateful where possible.

## Contracts to establish

1. Robot namespace mapping
   - `robotId` -> ROS2 namespace (`/robot_<id>` or similar convention)
2. Frame conventions
   - `map`, `world`, `base_link` alignment and transform assumptions
3. Command mapping
   - `robot.task.command` -> ROS2 action goal/service call
4. Feedback mapping
   - ROS2 action feedback/result -> `robot.task.status`

## Suggested implementation tracks

1. Bridge service skeleton
   - Standalone process/module with Kafka consumer/producer and ROS2 client nodes
2. Command adapters
   - Parse task command payload
   - Build ROS2 action goals
3. Feedback/result adapters
   - Convert ROS2 feedback to normalized task status messages
4. Failure handling
   - Map ROS2 execution errors into `FAILED` status with reason codes

## Observability requirements

- Log correlation via `correlationId` and `taskId`.
- Metrics for command accepted/rejected and bridge latency.
- Traceability from task creation to ROS2 completion.

## Exit criteria

- Task command emitted by control-plane is consumed by bridge and forwarded into ROS2 runtime.
- ROS2 feedback and result are reflected in `robot.task.status` and visible in control-plane task APIs.
