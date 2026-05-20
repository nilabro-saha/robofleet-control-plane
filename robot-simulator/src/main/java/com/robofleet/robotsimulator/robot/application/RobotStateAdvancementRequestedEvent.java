package com.robofleet.robotsimulator.robot.application;

import com.robofleet.robotsimulator.robot.domain.RobotActor;

/**
 * Event signaling that random state advancement scheduling should be registered for a robot actor.
 */
public record RobotStateAdvancementRequestedEvent(RobotActor robotActor) {
}
