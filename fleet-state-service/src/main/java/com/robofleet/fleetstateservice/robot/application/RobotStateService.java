package com.robofleet.fleetstateservice.robot.application;

import com.robofleet.fleetstateservice.robot.application.dto.RobotStateResponse;
import com.robofleet.fleetstateservice.robot.infrastructure.messaging.RobotTelemetryEvent;

import java.util.List;
import java.util.Optional;

public interface RobotStateService {

    void upsertFromTelemetry(RobotTelemetryEvent event);

    List<RobotStateResponse> getAllRobots();

    Optional<RobotStateResponse> getRobotById(String robotId);
}
