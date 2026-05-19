package com.robofleet.fleetstateservice.robot.application.dto;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;

@Value
@Builder
public class RobotStateResponse {
    String robotId;
    double x;
    double y;
    double battery;
    String status;
    Instant timestamp;
}
