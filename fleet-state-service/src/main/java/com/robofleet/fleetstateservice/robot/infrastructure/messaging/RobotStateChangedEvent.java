package com.robofleet.fleetstateservice.robot.infrastructure.messaging;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

/**
 * Incoming telemetry contract transported over Kafka.
 *
 * <p>Intent: define the minimum shared schema between telemetry producers
 * (simulator/robots) and the fleet state materializer.</p>
 */
@Value
@Builder
@Jacksonized
public class RobotStateChangedEvent {
  String robotId;
  @JsonProperty("x")
  double positionX;
  @JsonProperty("y")
  double positionY;
  double battery;
  String status;
  Instant timestamp;
}
