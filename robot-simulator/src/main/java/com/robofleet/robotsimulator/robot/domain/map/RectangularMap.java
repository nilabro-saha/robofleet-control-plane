package com.robofleet.robotsimulator.robot.domain.map;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Rectangular bounded map implementation for robot simulation.
 *
 * @param minX minimum allowed X coordinate in map space
 * @param maxX maximum allowed X coordinate in map space
 * @param minY minimum allowed Y coordinate in map space
 * @param maxY maximum allowed Y coordinate in map space
 */
public record RectangularMap(double minX, double maxX, double minY, double maxY) implements RobotMap {

  /**
   * Clamps X coordinate to this rectangular map boundary.
   *
   * @param x candidate x coordinate
   * @return bounded x coordinate
   */
  public double clampX(double x) {
    return Math.clamp(x, minX, maxX);
  }

  /**
   * Clamps Y coordinate to this rectangular map boundary.
   *
   * @param y candidate y coordinate
   * @return bounded y coordinate
   */
  public double clampY(double y) {
    return Math.clamp(y, minY, maxY);
  }

  @Override
  public MapLocation randomAvailableLocation(ThreadLocalRandom random) {
    return new MapLocation(
        random.nextDouble(minX, maxX),
        random.nextDouble(minY, maxY)
    );
  }
}
