package com.robofleet.fleetstateservice.robot.domain;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RobotStateTest {

  @Test
  void shouldBuildRobotStateWithExpectedValues() {
    Instant timestamp = Instant.parse("2026-05-19T16:40:03Z");

    RobotState state = RobotState.builder()
        .robotId("robot-1")
        .positionX(12.34)
        .positionY(56.78)
        .battery(87.1)
        .status("MOVING")
        .timestamp(timestamp)
        .build();

    assertEquals("robot-1", state.getRobotId());
    assertEquals(12.34, state.getPositionX());
    assertEquals(56.78, state.getPositionY());
    assertEquals(87.1, state.getBattery());
    assertEquals("MOVING", state.getStatus());
    assertEquals(timestamp, state.getTimestamp());
  }
}
