package com.robofleet.fleetstateservice.robot.infrastructure.messaging;

import com.robofleet.fleetstateservice.robot.application.RobotStateService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RobotTelemetryConsumerTest {

  @Mock
  private RobotStateService robotStateService;

  @InjectMocks
  private RobotTelemetryConsumer consumer;

  @Test
  void shouldDelegateConsumedEventToService() {
    RobotTelemetryEvent event = RobotTelemetryEvent.builder()
        .robotId("robot-1")
        .positionX(12.34)
        .positionY(56.78)
        .battery(87.1)
        .status("MOVING")
        .timestamp(Instant.parse("2026-05-19T16:40:03Z"))
        .build();

    consumer.consume(event);

    verify(robotStateService).upsertFromTelemetry(event);
  }

  @Test
  void shouldNotThrowWhenServiceFails() {
    RobotTelemetryEvent event = RobotTelemetryEvent.builder()
        .robotId("robot-1")
        .positionX(12.34)
        .positionY(56.78)
        .battery(87.1)
        .status("MOVING")
        .timestamp(Instant.parse("2026-05-19T16:40:03Z"))
        .build();
    doThrow(new RuntimeException("boom")).when(robotStateService).upsertFromTelemetry(event);

    consumer.consume(event);

    verify(robotStateService).upsertFromTelemetry(event);
  }
}
