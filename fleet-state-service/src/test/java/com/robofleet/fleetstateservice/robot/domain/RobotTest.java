package com.robofleet.fleetstateservice.robot.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class RobotTest {

  @Test
  void markAsActive_shouldSetLifecycleStatusToActive() {
    Robot robot = Robot.builder()
        .robotId("robot-1")
        .displayName("Alpha")
        .lifecycleStatus(RobotLifecycleStatus.CREATE_PENDING)
        .build();

    robot.markAsActive();

    assertEquals(RobotLifecycleStatus.ACTIVE, robot.getLifecycleStatus());
  }
}
