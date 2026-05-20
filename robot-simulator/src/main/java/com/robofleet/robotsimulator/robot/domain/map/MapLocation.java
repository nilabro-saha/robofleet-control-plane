package com.robofleet.robotsimulator.robot.domain.map;

/**
 * Immutable map coordinate used for robot placement.
 *
 * @param x x coordinate
 * @param y y coordinate
 */
public record MapLocation(double x, double y) {
}
