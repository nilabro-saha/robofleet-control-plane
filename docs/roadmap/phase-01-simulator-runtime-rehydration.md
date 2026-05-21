# Phase 1 — Simulator Runtime Rehydration

## Objective

Ensure robots already present in control-plane persistence are recreated in simulator runtime after restarts so they resume live behavior and telemetry.

## Current status

In place (implemented in current codebase):

- Startup publisher emits `REHYDRATE_ACTIVE` lifecycle events for ACTIVE robots.
- Simulator lifecycle handler accepts both `CREATE_PENDING` and `REHYDRATE_ACTIVE`.
- Registration logic avoids duplicate runtime actors.
- Lifecycle roundtrip (`CREATED`/`REMOVED`) is consumed by fleet-state-service.

## Scope and deliverables

1. Startup rehydration trigger in control plane
   - Query ACTIVE robots
   - Publish rehydration command per robot
2. Simulator-side command handling
   - Register robot actor if missing
   - Ignore duplicates safely
3. State continuity behavior
   - Resume telemetry publishing for rehydrated robots
   - Maintain lifecycle consistency in API projections
4. Test coverage
   - Unit tests for event publishing and handler behavior
   - Restart-path integration verification

## Work breakdown (reference)

- `fleet-state-service`
  - `ActiveRobotRehydrationPublisher`
  - `LifecycleEventType.REHYDRATE_ACTIVE`
- `robot-simulator`
  - `CreatePendingLifecycleEventHandler`
  - `RobotSimulationOrchestrator.registerRobot()` idempotency

## Exit criteria

- After service restart, previously ACTIVE robots become live again without manual recreation.
- No duplicate runtime actors are created for the same `robotId`.
- Dashboard receives fresh telemetry for rehydrated robots.

## Follow-up hardening (post-phase)

- Add retry/backoff strategy for startup publishing failures.
- Add explicit dead-letter handling path for lifecycle command failures.
