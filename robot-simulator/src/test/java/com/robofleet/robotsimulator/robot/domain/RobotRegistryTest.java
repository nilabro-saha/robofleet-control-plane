package com.robofleet.robotsimulator.robot.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import org.junit.jupiter.api.Test;

class RobotRegistryTest {

  @Test
  void register_shouldIncludeRobotInSnapshot() {
    RobotRegistry robotRegistry = new RobotRegistry();
    RobotActor robotActor = RobotActor.builder()
        .robotId("robot-1")
        .positionX(1.0)
        .positionY(2.0)
        .battery(50.0)
        .status(RobotStatus.IDLE)
        .build();

    robotRegistry.register(robotActor);

    List<RobotActor> registeredRobots = robotRegistry.getRegisteredRobots();
    assertEquals(1, registeredRobots.size());
    assertEquals("robot-1", registeredRobots.getFirst().getRobotId());
  }

  @Test
  void getRegisteredRobots_shouldReturnImmutableSnapshot() {
    RobotRegistry robotRegistry = new RobotRegistry();
    RobotActor robotActor = RobotActor.builder()
        .robotId("robot-2")
        .positionX(1.0)
        .positionY(2.0)
        .battery(50.0)
        .status(RobotStatus.IDLE)
        .build();
    robotRegistry.register(robotActor);

    List<RobotActor> registeredRobots = robotRegistry.getRegisteredRobots();

    assertThrows(UnsupportedOperationException.class, () -> registeredRobots.add(robotActor));
  }
}
