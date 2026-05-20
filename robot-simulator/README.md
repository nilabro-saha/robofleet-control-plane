# Robot Simulator Module

## Intent

This module is now a **Spring Boot robot actor simulator** for the edge side of RoboFleet.

It models each robot as an in-memory actor with internal mutable state and registers telemetry tasks into a shared Spring-managed thread pool.

## What it does

- Spawns configurable robot actors at startup (default: 3)
- Places all robots on a bounded rectangular map registry
- Initializes each robot with random:
  - location (`x`, `y`)
  - battery charge
  - status (`IDLE`, `MOVING`, `CHARGING`, `ERROR`)
- Runs one telemetry loop per robot using a shared task executor
- Uses a shared scheduled thread pool (no `Thread.sleep`) for periodic telemetry
- Publishes telemetry to Kafka at a configurable interval (default: 1000 ms)

## Instrumentation flow

Instrumentation is orchestrated at the application layer:

1. `RobotFleetBootstrap` triggers `RobotSimulationOrchestrator` at `ApplicationReadyEvent`
2. Orchestrator spawns robot actors and registers them in `RobotRegistry`
3. Orchestrator publishes two events per robot:
   - `RobotTelemetryInstrumentationRequestedEvent`
   - `RobotStateAdvancementRequestedEvent`
4. Dedicated listeners register fixed-rate schedules for:
   - telemetry publishing
   - random state advancement

This keeps domain entities focused on state/behavior while orchestration stays in the application layer.

## Runtime contract

- Broker: `localhost:9092`
- Topic: `robot.telemetry`
- Timestamp format: UTC ISO-8601 (Instant compatible)

## Configuration

From `src/main/resources/application.properties`:

- `robot.simulator.robot-count=3`
- `robot.simulator.telemetry-interval-ms=1000`
- `robot.simulator.map-min-x=0`
- `robot.simulator.map-max-x=100`
- `robot.simulator.map-min-y=0`
- `robot.simulator.map-max-y=100`
- `robot.simulator.kafka.topic=robot.telemetry`

## Run

```bash
cd robot-simulator
mvn spring-boot:run
```
