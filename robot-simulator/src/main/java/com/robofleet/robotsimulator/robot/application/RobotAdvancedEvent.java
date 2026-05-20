package com.robofleet.robotsimulator.robot.application;

import com.robofleet.robotsimulator.robot.domain.RobotActor;

/**
 * Spring event emitted when a robot advances state.
 */
public record RobotAdvancedEvent(RobotActor robotActor) {
}
