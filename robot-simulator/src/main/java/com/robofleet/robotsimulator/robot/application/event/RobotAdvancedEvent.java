package com.robofleet.robotsimulator.robot.application.event;

import com.robofleet.robotsimulator.robot.domain.model.RobotState;

/**
 * Spring event emitted when a robot advances state.
 *
 * @author Nilabro Saha
 */
public record RobotAdvancedEvent(RobotState robotView, String correlationId) {

  public RobotAdvancedEvent(RobotState robotView) {
    this(robotView, null);
  }
}
