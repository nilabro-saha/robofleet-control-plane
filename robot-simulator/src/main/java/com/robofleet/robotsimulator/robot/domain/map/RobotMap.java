package com.robofleet.robotsimulator.robot.domain.map;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Sealed map abstraction for robot simulation environments.
 */
public sealed interface RobotMap permits RectangularMap {

  /**
   * Returns a random available location on this map.
   *
   * @param random random source
   * @return random available coordinate
   */
  MapLocation randomAvailableLocation(ThreadLocalRandom random);
}
