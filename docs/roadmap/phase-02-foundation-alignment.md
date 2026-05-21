# Phase 2 — Foundation Alignment

## Objective

Prepare the current MVP architecture for task orchestration by tightening contracts, topic semantics, and lifecycle/state conventions without breaking existing robot telemetry flows.

## Why this phase matters

Phase 3 (task domain) will add new APIs, entities, and Kafka topics. If message contracts, IDs, and naming conventions are not standardized now, task features will create brittle integration points and migration overhead.

## Current status

Partially complete:

- Robot lifecycle and telemetry pipelines are in place.
- Rehydration lifecycle event (`REHYDRATE_ACTIVE`) is implemented.
- APIs currently expose both `/api/robot-statuses` and `/api/robots` surfaces.

Not yet complete:

- No formal event contract/versioning documentation for all topics.
- No standardized correlation-id / causation-id policy implemented end-to-end.
- No task lifecycle enum/state model yet.
- Naming conventions between API projections and topics need consolidation guidance.

## Detailed target outcomes

1. Contract governance baseline
   - Define canonical JSON contracts for:
     - `robot.telemetry`
     - `robot.lifecycle`
     - reserved contracts for `robot.task.command` and `robot.task.status`
   - Add schema version field convention (for example: `schemaVersion`).

2. Message traceability conventions
   - Introduce envelope metadata strategy:
     - `eventId`
     - `correlationId`
     - `causationId`
     - `emittedAt`
     - `producer`
   - Document propagation rules across services.

3. Lifecycle and status taxonomy cleanup
   - Freeze robot lifecycle/status semantics for current modules.
   - Predefine task lifecycle states for Phase 3 compatibility.
   - Clarify transition rules and invalid transitions.

4. API contract alignment
   - Confirm long-term API strategy for:
     - `GET /api/robot-statuses` (state projection)
     - `GET /api/robots` (identity/lifecycle projection)
   - Document intended use by UI vs operator/automation clients.

5. Compatibility and safety net
   - Add compatibility tests for event contract changes.
   - Define backward-compatibility policy for consumers.

## Workstreams and concrete tasks

### Workstream A — Contract docs and schema evolution

- Create docs under `docs/contracts/`:
  - `robot-telemetry-v1.md`
  - `robot-lifecycle-v1.md`
  - `task-command-v1-draft.md`
  - `task-status-v1-draft.md`
- Include field-level type requirements and examples.
- Define additive-change policy and deprecation approach.

### Workstream B — Event envelope introduction

- Decide implementation approach:
  - augment existing DTO payloads directly, or
  - introduce wrapper envelope object with payload.
- Update producer paths in:
  - `robot-simulator` lifecycle/telemetry publishers
  - `fleet-state-service` lifecycle command publisher
- Update consumers to parse/validate metadata.

### Workstream C — State machine documentation

- Publish state transition tables:
  - robot lifecycle (`CREATE_PENDING`, `REHYDRATE_ACTIVE`, `CREATED`, `DELETE_PENDING`, `REMOVED`)
  - robot runtime status (`IDLE`, `MOVING`, `CHARGING`, `ERROR`, etc.)
  - task lifecycle draft (`QUEUED`, `ASSIGNED`, `RUNNING`, `COMPLETED`, `FAILED`, `CANCELLED`)

### Workstream D — API naming and usage policy

- Document stable API surface and intended consumers.
- Decide if both endpoints remain first-class or one becomes an internal projection route.
- Reflect decisions in `fleet-dashboard-ui/README.md` and root `README.md`.

### Workstream E — Contract compatibility tests

- Add tests verifying:
  - mandatory fields present in emitted events
  - enum compatibility handling for unknown/new values
  - consumer resilience to additive fields

## Suggested deliverables checklist

- [x] Contract docs committed and linked from root README
  - Comment (22-May-26 12:43 am IST): `docs/contracts/*` is present and linked from root README.
- [ ] Correlation/causation strategy documented and implemented
  - Comment (22-May-26 12:43 am IST): `correlationId` is implemented across active lifecycle/telemetry paths; `eventId` and `causationId` are still deferred.
- [x] Lifecycle/state transition tables approved
  - Comment (22-May-26 12:43 am IST): Robot lifecycle/status and draft task lifecycle semantics are documented in contracts/roadmap docs and reflected in current enums.
- [ ] API naming policy finalized and documented
  - Comment (22-May-26 12:43 am IST): Endpoints are implemented (`/api/robot-statuses` and `/api/robots`), but module-level docs still need one canonical policy statement.
- [ ] Compatibility tests green in CI/local verification
  - Comment (22-May-26 12:43 am IST): Core flow tests are green locally, but explicit additive/unknown-field compatibility tests are still missing.

## Exit criteria

- Contract docs are versioned and discoverable.
- Both services can emit/consume events with trace metadata.
- No regression in current create/delete/rehydration and telemetry flows.
- Team has a stable foundation to begin Phase 3 task-domain implementation.

## Risks and mitigations

- Risk: Breaking existing consumers during envelope changes.
  - Mitigation: staged rollout with compatibility mode and dual-field support.
- Risk: Ambiguous API ownership across `/robots` vs `/robot-statuses`.
  - Mitigation: explicit endpoint ownership/intent docs and UI contract lock.
- Risk: Over-design delaying Phase 3.
  - Mitigation: time-box to minimum viable conventions and defer non-critical standardization.

## Recommended first sprint for Phase 2

1. Ship contract docs + transition tables.
2. Add `eventId/correlationId/causationId` to lifecycle events only.
3. Add compatibility tests for lifecycle consumers.
4. Confirm API naming policy and document it.
