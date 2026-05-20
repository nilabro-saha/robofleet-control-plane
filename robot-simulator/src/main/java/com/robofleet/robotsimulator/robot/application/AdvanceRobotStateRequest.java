package com.robofleet.robotsimulator.robot.application;

import com.robofleet.robotsimulator.robot.domain.AdvancementMode;
import com.robofleet.robotsimulator.robot.domain.RobotActor;

/**
 * Event requesting a single robot state advancement cycle.
 */
public record AdvanceRobotStateRequest(RobotActor robotActor, AdvancementMode advancementMode) {
}
