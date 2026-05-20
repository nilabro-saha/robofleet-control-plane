package com.robofleet.fleetstateservice.robot.application.dto;

import lombok.Builder;
import lombok.Value;

/**
 * Command payload to request creation of a simulator robot.
 */
@Value
@Builder
public class CreateRobotRequest {
  String displayName;
}
