package com.robofleet.fleetstateservice.robot.infrastructure.messaging;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.time.Instant;

@Value
@Builder
@Jacksonized
public class RobotTelemetryEvent {
    String robotId;
    double x;
    double y;
    double battery;
    String status;
    Instant timestamp;
}
