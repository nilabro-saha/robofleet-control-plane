package com.robofleet.robotsimulator.robot.infrastructure.messaging;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

/**
 * Lifecycle contract emitted to Kafka for create/remove state changes.
 */
@Value
@Builder
@Jacksonized
public class RobotLifecycleEvent {
  String robotId;
  @JsonProperty("x")
  double positionX;
  @JsonProperty("y")
  double positionY;
  double battery;
  String status;
  String eventType;
  Instant timestamp;
}
