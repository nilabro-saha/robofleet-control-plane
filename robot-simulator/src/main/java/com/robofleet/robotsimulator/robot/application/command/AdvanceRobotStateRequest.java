package com.robofleet.robotsimulator.robot.application.command;

import com.robofleet.robotsimulator.robot.domain.behavior.AdvancementMode;
import com.robofleet.robotsimulator.robot.domain.model.RobotActor;

/**
 * Event requesting a single robot state advancement cycle.
 */
public record AdvanceRobotStateRequest(RobotActor robotActor, AdvancementMode advancementMode) {
}
