package com.robofleet.robotsimulator.robot.domain.map;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.ThreadLocalRandom;
import org.junit.jupiter.api.Test;

class RectangularMapTest {

  @Test
  void clampXAndClampY_shouldKeepValuesWithinBounds() {
    RectangularMap map = new RectangularMap(0.0, 10.0, 2.0, 8.0);

    assertTrue(map.clampX(-1.0) >= 0.0);
    assertTrue(map.clampX(12.0) <= 10.0);
    assertTrue(map.clampY(0.0) >= 2.0);
    assertTrue(map.clampY(12.0) <= 8.0);
  }

  @Test
  void randomAvailableLocation_shouldReturnCoordinateInsideMapBounds() {
    RectangularMap map = new RectangularMap(5.0, 15.0, 20.0, 30.0);

    for (int i = 0; i < 100; i++) {
      MapLocation mapLocation = map.randomAvailableLocation(ThreadLocalRandom.current());

      assertTrue(mapLocation.x() >= 5.0 && mapLocation.x() <= 15.0);
      assertTrue(mapLocation.y() >= 20.0 && mapLocation.y() <= 30.0);
    }
  }
}
