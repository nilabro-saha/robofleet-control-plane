package com.robofleet.robotsimulator.robot.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.robofleet.robotsimulator.robot.domain.behavior.RandomAdvance;
import com.robofleet.robotsimulator.robot.domain.map.RectangularMap;

import java.util.concurrent.ThreadLocalRandom;
import org.junit.jupiter.api.Test;

class RobotActorTest {

  @Test
  void lastStateView_shouldReflectCurrentState() {
    RobotActor robotActor = RobotActor.builder()
        .robotId("robot-1")
        .positionX(10.126)
        .positionY(20.994)
        .battery(55.678)
        .status(RobotStatus.IDLE)
        .build();

    RobotState lastStateView = robotActor.lastState();

    assertEquals("robot-1", lastStateView.robotId());
    assertEquals(10.126, lastStateView.positionX());
    assertEquals(20.994, lastStateView.positionY());
    assertEquals(55.678, lastStateView.battery());
    assertEquals(RobotStatus.IDLE, lastStateView.status());
  }

  @Test
  void advanceState_shouldKeepPositionAndBatteryWithinBounds() {
    RobotActor robotActor = RobotActor.builder()
        .robotId("robot-2")
        .positionX(99.9)
        .positionY(99.9)
        .battery(50.0)
        .status(RobotStatus.MOVING)
        .build();
    RectangularMap map = new RectangularMap(0.0, 100.0, 0.0, 100.0);

    for (int i = 0; i < 200; i++) {
      robotActor.advanceState(map, new RandomAdvance(ThreadLocalRandom.current()));
      RobotState lastStateView = robotActor.lastState();
      assertTrue(lastStateView.positionX() >= 0.0 && lastStateView.positionX() <= 100.0);
      assertTrue(lastStateView.positionY() >= 0.0 && lastStateView.positionY() <= 100.0);
      assertTrue(lastStateView.battery() >= 0.0 && lastStateView.battery() <= 100.0);
      assertNotNull(robotActor.getStatus());
    }
  }

  @Test
  void advanceState_shouldThrowForUnsupportedMapType() {
    RobotActor robotActor = RobotActor.builder()
        .robotId("robot-3")
        .positionX(5.0)
        .positionY(5.0)
        .battery(80.0)
        .status(RobotStatus.IDLE)
        .build();

    assertThrows(
        IllegalStateException.class,
        () -> robotActor.advanceState(null, new RandomAdvance(ThreadLocalRandom.current())));
  }
}
