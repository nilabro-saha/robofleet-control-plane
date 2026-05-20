package com.robofleet.robotsimulator.robot.application.event;

import com.robofleet.robotsimulator.robot.domain.model.RobotActor;

/**
 * Spring event emitted when a robot is created/registered.
 */
public record RobotCreatedEvent(RobotActor robotActor) {
}
