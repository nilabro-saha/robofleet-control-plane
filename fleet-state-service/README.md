# Fleet State Service Module

## Intent

This module is the **state materialization layer** of the MVP.

Its role is to transform a stream of robot telemetry events into a simple, queryable view of fleet state for operators and downstream tools.

In other words: this service answers **"what is the latest known state of each robot right now?"**

## Why this exists in the MVP

Kafka is excellent for event flow, but operators and dashboards need low-latency read endpoints.
This service bridges that gap by consuming the event stream and continuously upserting latest state.

## Responsibilities

- Consume `robot.telemetry` events
- Upsert latest state per `robotId` in SQLite
- Expose API endpoints:
  - `GET /api/robots`
  - `GET /api/robots/{id}`

## API query behavior

`GET /api/robots` supports Spring pageable query params:

- `sort=<field>,<direction>`
  - example: `sort=battery,desc`
- `size=<number>`
- `page=<number>`

Default behavior:

- `size=100`
- `sort=robotId,asc`

Supported sort fields for this endpoint:

- `robotId`
- `positionX`
- `positionY`
- `battery`
- `status`
- `timestamp`

## Design note

This service intentionally stores only the latest state (not full history), keeping the MVP simple and fast to reason about.

## Local Pact contract testing

This module participates in both API and event pact flows.

### Classes in this module

- Consumer-side pact generation (`pact-generate` profile):
  - `com.robofleet.fleetstateservice.contract.RobotApiPactVerifier`
  - `com.robofleet.fleetstateservice.contract.RobotEventPactVerifier`
- Provider-side pact verification (`pact-verify` profile):
  - `com.robofleet.fleetstateservice.contract.RobotApiPactProvider`
  - `com.robofleet.fleetstateservice.contract.RobotEventPactProvider`

### Pact location

- Shared top-level pact folder: `../pacts` (from module root)

### Run locally (module-level)

Generate this module's consumer pacts:

```bash
mvn -Ppact-generate test
```

Verify this module as pact provider:

```bash
mvn -Ppact-verify test
```

### Run full cross-module pact flow (recommended)

From repository root:

```bash
mvn -f pom.xml verify
```

This root Maven flow executes:

1. Fleet consumer pact generation
2. Simulator consumer pact generation
3. Fleet provider verification
4. Simulator provider verification
