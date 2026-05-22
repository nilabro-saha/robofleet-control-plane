package com.robofleet.fleetstateservice.robot.application.dto;

import com.robofleet.fleetstateservice.robot.domain.RobotLifecycleStatus;
import lombok.Builder;
import lombok.Value;

/**
 * API projection for robot identity/lifecycle metadata.
 *
 * @author Nilabro Saha
 */
@Value
@Builder
public class RobotSummaryResponse {
  String robotId;
  String displayName;
  RobotLifecycleStatus lifecycleStatus;
}
