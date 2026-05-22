package com.robofleet.robotsimulator.robot.application.event;

import com.robofleet.robotsimulator.robot.domain.model.RobotActor;

/**
 * Spring event emitted when a robot advances state.
 *
 * @author Nilabro Saha
 */
public record RobotAdvancedEvent(RobotActor robotActor) {
}
