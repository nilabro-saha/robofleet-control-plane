package com.robofleet.fleetstateservice.robot.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.robofleet.fleetstateservice.robot.domain.RobotLifecycleStatus;
import java.time.Instant;
import lombok.Builder;
import lombok.Value;

/**
 * API projection of latest robot state.
 *
 * <p>Intent: provide UI-friendly, immutable data representing what operators
 * need to understand current robot health and position.</p>
 *
 * @author Nilabro Saha
 */
@Value
@Builder
public class RobotStateResponse {
  @SortableFieldMapping(entityAlias = "robot")
  String robotId;

  @SortableFieldMapping(entityAlias = "robot")
  String displayName;

  @SortableFieldMapping(entityAlias = "state")
  @JsonProperty("x")
  double positionX;

  @SortableFieldMapping(entityAlias = "state")
  @JsonProperty("y")
  double positionY;

  @SortableFieldMapping(entityAlias = "state")
  double battery;

  @SortableFieldMapping(entityAlias = "robot")
  RobotLifecycleStatus lifecycleStatus;

  @SortableFieldMapping(entityAlias = "state")
  String status;

  @SortableFieldMapping(entityAlias = "state")
  Instant timestamp;
}
