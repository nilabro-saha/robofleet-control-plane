# Event Contract Specifications

This folder contains the event contract docs for the RoboFleet control plane.
Each contract is versioned so we can evolve safely without breaking consumers.

## Current specs

- [robot-telemetry-v1](./robot-telemetry-v1.md)
- [robot-lifecycle-v1](./robot-lifecycle-v1.md)
- [robot-task-command-v1-draft](./robot-task-command-v1-draft.md)
- [robot-task-status-v1-draft](./robot-task-status-v1-draft.md)

## Metadata policy (Phase 2 baseline)

- `correlationId` is required for all newly emitted events.
- `eventName` is required for all newly emitted events (kebab-case).
- `eventId` and `causationId` are planned for a later phase.
- Consumers should ignore unknown additive fields.

## Versioning policy

- Prefer additive changes when possible (backward-compatible).
- If a change is breaking, publish a new versioned contract doc.
