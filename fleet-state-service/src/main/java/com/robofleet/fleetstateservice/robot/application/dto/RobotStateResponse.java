package com.robofleet.fleetstateservice.robot.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import lombok.Builder;
import lombok.Value;

/**
 * API projection of latest robot state.
 *
 * <p>Intent: provide UI-friendly, immutable data representing what operators
 * need to understand current robot health and position.</p>
 */
@Value
@Builder
public class RobotStateResponse {
  String robotId;
  @JsonProperty("x")
  double positionX;
  @JsonProperty("y")
  double positionY;
  double battery;
  String status;
  Instant timestamp;
}
