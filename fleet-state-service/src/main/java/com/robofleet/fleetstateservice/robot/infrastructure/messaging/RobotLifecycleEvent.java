package com.robofleet.fleetstateservice.robot.infrastructure.messaging;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

/**
 * Lifecycle contract for create/remove robot events transported over Kafka.
 */
@Value
@Builder
@Jacksonized
public class RobotLifecycleEvent {
  String robotId;
  @JsonProperty("x")
  Double positionX;
  @JsonProperty("y")
  Double positionY;
  Double battery;
  String status;
  LifecycleEventType eventType;
  Instant timestamp;
}
