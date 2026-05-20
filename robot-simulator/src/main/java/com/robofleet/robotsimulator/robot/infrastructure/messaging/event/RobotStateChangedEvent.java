package com.robofleet.robotsimulator.robot.infrastructure.messaging.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

/**
 * Outgoing telemetry contract emitted to Kafka.
 */
@Value
@Builder
@Jacksonized
public class RobotStateChangedEvent {
  String robotId;
  String displayName;
  @JsonProperty("x")
  double positionX;
  @JsonProperty("y")
  double positionY;
  double battery;
  String status;
  Instant timestamp;
}
