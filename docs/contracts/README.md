# Event Contract Specifications

This directory contains versioned event contract docs for the RoboFleet control plane.

## Current specs

- [robot-telemetry-v1](./robot-telemetry-v1.md)
- [robot-lifecycle-v1](./robot-lifecycle-v1.md)
- [robot-task-command-v1-draft](./robot-task-command-v1-draft.md)
- [robot-task-status-v1-draft](./robot-task-status-v1-draft.md)

## Metadata policy (Phase 2 baseline)

- `correlationId` is mandatory for newly emitted events.
- `eventName` is mandatory for newly emitted events (kebab-case).
- `eventId` and `causationId` are deferred to a later phase.
- Consumers should tolerate unknown additive fields.

## Versioning policy

- Additive fields are backward-compatible and preferred.
- Breaking schema changes require a new versioned contract doc.
