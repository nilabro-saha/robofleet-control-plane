package com.robofleet.robotsimulator.robot.application.event;

import com.robofleet.robotsimulator.robot.domain.model.RobotState;

/**
 * Spring event emitted when a robot is deleted/deregistered.
 *
 * @author Nilabro Saha
 */
public record RobotDeletedEvent(RobotState robotView, String correlationId) {

  public RobotDeletedEvent(RobotState robotView) {
    this(robotView, null);
  }
}
