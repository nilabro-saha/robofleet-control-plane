package com.robofleet.fleetstateservice.robot.application.dto;

/**
 * API response for robot creation orchestration requests.
 */
public record RobotCreationResponse(
    String robotId,
    String displayName,
    String lifecycleStatus
) {
}
