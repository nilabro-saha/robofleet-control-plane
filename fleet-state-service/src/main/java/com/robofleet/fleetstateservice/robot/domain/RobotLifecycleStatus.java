package com.robofleet.fleetstateservice.robot.domain;

/**
 * Lifecycle status for a robot in control-plane orchestration.
 *
 * @author Nilabro Saha
 */
public enum RobotLifecycleStatus {
  CREATE_PENDING,
  ACTIVE,
  DELETE_PENDING
}
