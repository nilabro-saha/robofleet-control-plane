package com.robofleet.robotsimulator.robot.infrastructure.messaging;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;

import com.robofleet.robotsimulator.robot.application.RobotOrchestrator;
import com.robofleet.robotsimulator.robot.application.actor.RobotOrchestration;
import com.robofleet.robotsimulator.robot.infrastructure.messaging.event.LifecycleEventType;
import com.robofleet.robotsimulator.robot.infrastructure.messaging.event.RobotLifecycleEvent;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CreatePendingLifecycleEventHandlerTest {

  @Mock
  private RobotOrchestrator robotOrchestrator;

  @InjectMocks
  private CreatePendingLifecycleEventHandler handler;

  @Test
  void canHandle_shouldReturnTrueForCreatePending() {
    RobotLifecycleEvent event = RobotLifecycleEvent.builder()
        .robotId("robot-1")
        .eventType(LifecycleEventType.CREATE_PENDING)
        .timestamp(Instant.parse("2026-05-20T10:00:00Z"))
        .build();

    assertTrue(handler.canHandle(event));
  }

  @Test
  void canHandle_shouldReturnFalseForOtherLifecycleTypes() {
    RobotLifecycleEvent event = RobotLifecycleEvent.builder()
        .robotId("robot-1")
        .eventType(LifecycleEventType.DELETE_PENDING)
        .timestamp(Instant.parse("2026-05-20T10:00:00Z"))
        .build();

    assertFalse(handler.canHandle(event));
  }

  @Test
  void canHandle_shouldReturnFalseForRehydrateActive() {
    RobotLifecycleEvent event = RobotLifecycleEvent.builder()
        .robotId("robot-1")
        .eventType(LifecycleEventType.REHYDRATE_ACTIVE)
        .timestamp(Instant.parse("2026-05-20T10:00:00Z"))
        .build();

    assertFalse(handler.canHandle(event));
  }

  @Test
  void handleEvent_shouldSendSpawnRandomCommandToRegistry() {
    RobotLifecycleEvent event = RobotLifecycleEvent.builder()
        .correlationId("corr-create-42")
        .robotId("robot-42")
        .eventType(LifecycleEventType.CREATE_PENDING)
        .timestamp(Instant.parse("2026-05-20T10:00:00Z"))
        .build();

    handler.handleEvent(event);

    verify(robotOrchestrator).tell(new RobotOrchestration.SpawnRandom("robot-42", "corr-create-42"));
  }
}
