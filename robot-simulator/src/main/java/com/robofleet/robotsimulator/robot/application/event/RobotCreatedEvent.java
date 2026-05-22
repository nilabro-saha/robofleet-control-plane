package com.robofleet.robotsimulator.robot.application.event;

import com.robofleet.robotsimulator.robot.domain.model.RobotActor;

/**
 * Spring event emitted when a robot is created/registered.
 *
 * @author Nilabro Saha
 */
public record RobotCreatedEvent(RobotActor robotActor) {
}
