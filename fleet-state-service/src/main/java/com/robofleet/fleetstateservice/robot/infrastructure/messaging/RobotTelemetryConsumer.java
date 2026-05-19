package com.robofleet.fleetstateservice.robot.infrastructure.messaging;

import com.robofleet.fleetstateservice.robot.application.RobotStateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class RobotTelemetryConsumer {

    private final RobotStateService robotStateService;

    @KafkaListener(topics = "robot.telemetry", groupId = "fleet-state-service")
    public void consume(RobotTelemetryEvent event) {
        try {
            robotStateService.upsertFromTelemetry(event);
            log.info("Telemetry consumed for robotId={}", event.getRobotId());
        } catch (Exception e) {
            log.error("Failed to process telemetry event for robotId={} ", event.getRobotId(), e);
        }
    }
}
