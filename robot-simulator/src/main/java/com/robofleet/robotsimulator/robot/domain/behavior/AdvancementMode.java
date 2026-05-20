package com.robofleet.robotsimulator.robot.domain.behavior;

/**
 * Marker for robot state advancement strategies.
 */
public sealed interface AdvancementMode permits RandomAdvance, PathFollowingAdvance,
    HoldPositionAdvance {
}
