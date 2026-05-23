package com.robofleet.robotsimulator.robot.application.actor;

import com.robofleet.robotsimulator.robot.domain.behavior.AdvancementMode;

/**
 * Marker contract for messages handled by simulator robot actor references.
 *
 * <p>Intent: provide one extensible message protocol so scheduler and lifecycle adapters can
 * submit robot work without direct mutable state access.</p>
 *
 * @author Nilabro Saha
 */
public sealed interface RobotCommand permits RobotCommand.AdvanceState {

  /**
   * Command instructing one robot actor reference to advance state once.
   */
  record AdvanceState(
      AdvancementMode advancementMode,
      String correlationId
  ) implements RobotCommand {

    public AdvanceState(AdvancementMode advancementMode) {
      this(advancementMode, null);
    }
  }
}
