package com.robofleet.robotsimulator.robot.application.actor;

import com.robofleet.robotsimulator.robot.domain.model.RobotStatus;

/**
 * State payload describing desired initial robot state.
 *
 * <p>All fields are explicit and non-null. Randomized spawn should use dedicated commands.</p>
 *
 * @param robotId required robot identifier
 * @param positionX initial X coordinate
 * @param positionY initial Y coordinate
 * @param battery initial battery
 * @param status initial status
 * @author Nilabro Saha
 */
public record RobotState(
    String robotId,
    double positionX,
    double positionY,
    double battery,
    RobotStatus status
) {
}
