package com.robofleet.robotsimulator.robot.application;

import com.robofleet.robotsimulator.robot.domain.RobotActor;

/**
 * Spring event emitted when a robot is deleted/deregistered.
 */
public record RobotDeletedEvent(RobotActor robotActor) {
}
