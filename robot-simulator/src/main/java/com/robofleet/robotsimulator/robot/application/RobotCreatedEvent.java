package com.robofleet.robotsimulator.robot.application;

import com.robofleet.robotsimulator.robot.domain.RobotActor;

/**
 * Spring event emitted when a robot is created/registered.
 */
public record RobotCreatedEvent(RobotActor robotActor) {
}
