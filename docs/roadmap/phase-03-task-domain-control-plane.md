# Phase 3 — Task Domain in Control Plane

## Objective

Add first-class task orchestration to the control plane so operators can create, track, and manage robot tasks end-to-end.

## Scope

- Task entity model and persistence
- Task APIs
- Kafka command/status integration
- Task lifecycle state machine

## Proposed task model (v1)

- `taskId`
- `taskType` (start with `PICK_AND_PLACE`)
- `pickupPose` (`x`, `y`, optional `z`, `frame`)
- `dropPose` (`x`, `y`, optional `z`, `frame`)
- `robotId` (nullable when queued)
- `priority`
- `status`
- `createdAt`, `startedAt`, `completedAt`
- `failureReason`

## APIs (minimum viable)

- `POST /api/tasks`
- `GET /api/tasks`
- `GET /api/tasks/{id}`
- `POST /api/tasks/{id}/cancel` (optional in first cut)

## Messaging integration

- Publish `robot.task.command` when task transitions to ASSIGNED.
- Consume `robot.task.status` and update persisted task state.
- Enforce idempotent status updates by `(taskId, eventId)`.

## Lifecycle draft

`QUEUED -> ASSIGNED -> RUNNING -> COMPLETED | FAILED | CANCELLED`

## Work breakdown

1. Domain + persistence
   - Add task aggregate/entity and repository
   - Add DB migration/init strategy for task table
2. Application layer
   - Task service for create/list/get/cancel/transition
3. API layer
   - Controller + DTOs + validation + error mapping
4. Messaging layer
   - Task command publisher
   - Task status consumer
5. Tests
   - Unit tests for transitions and validation
   - Integration test for command publish + status consume

## Exit criteria

- Operators can create tasks via API.
- Tasks are persisted and visible through list/detail APIs.
- Status events drive transitions to terminal states.
