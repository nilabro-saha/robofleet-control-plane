package com.robofleet.robotsimulator.robot.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.robofleet.robotsimulator.robot.domain.map.RectangularMap;
import com.robofleet.robotsimulator.robot.infrastructure.messaging.RobotTelemetryEvent;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class RobotActorTest {

  @Test
  void currentTelemetry_shouldReflectCurrentState() {
    RobotActor robotActor = RobotActor.builder()
        .robotId("robot-1")
        .positionX(10.126)
        .positionY(20.994)
        .battery(55.678)
        .status(RobotStatus.IDLE)
        .build();

    RobotTelemetryEvent telemetryEvent = robotActor.currentTelemetry();

    assertEquals("robot-1", telemetryEvent.getRobotId());
    assertEquals(10.13, telemetryEvent.getPositionX());
    assertEquals(20.99, telemetryEvent.getPositionY());
    assertEquals(55.68, telemetryEvent.getBattery());
    assertEquals("IDLE", telemetryEvent.getStatus());
    assertNotNull(telemetryEvent.getTimestamp());
    assertTrue(telemetryEvent.getTimestamp().isBefore(Instant.now().plusSeconds(1)));
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
      robotActor.advanceState(map);
      RobotTelemetryEvent telemetryEvent = robotActor.currentTelemetry();
      assertTrue(telemetryEvent.getPositionX() >= 0.0 && telemetryEvent.getPositionX() <= 100.0);
      assertTrue(telemetryEvent.getPositionY() >= 0.0 && telemetryEvent.getPositionY() <= 100.0);
      assertTrue(telemetryEvent.getBattery() >= 0.0 && telemetryEvent.getBattery() <= 100.0);
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

    assertThrows(IllegalStateException.class, () -> robotActor.advanceState(null));
  }
}
