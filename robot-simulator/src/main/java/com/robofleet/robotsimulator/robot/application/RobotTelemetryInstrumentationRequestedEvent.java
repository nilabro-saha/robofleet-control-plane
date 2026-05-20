package com.robofleet.robotsimulator.robot.application;

import com.robofleet.robotsimulator.robot.domain.RobotActor;

/**
 * Event signaling that telemetry scheduling should be registered for a robot actor.
 */
public record RobotTelemetryInstrumentationRequestedEvent(RobotActor robotActor) {
}
