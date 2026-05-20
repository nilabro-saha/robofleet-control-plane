package com.robofleet.fleetstateservice.robot.infrastructure.messaging;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;

import com.robofleet.fleetstateservice.robot.application.RobotStateService;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CreatedLifecycleEventHandlerTest {

  @Mock
  private RobotStateService robotStateService;

  @InjectMocks
  private CreatedLifecycleEventHandler handler;

  @Test
  void canHandle_shouldReturnTrueForCreatedEvent() {
    RobotLifecycleEvent event = RobotLifecycleEvent.builder()
        .robotId("robot-1")
        .eventType(LifecycleEventType.CREATED)
        .timestamp(Instant.parse("2026-05-19T16:40:03Z"))
        .build();

    assertTrue(handler.canHandle(event));
  }

  @Test
  void canHandle_shouldReturnFalseForOtherEventTypes() {
    RobotLifecycleEvent event = RobotLifecycleEvent.builder()
        .robotId("robot-1")
        .eventType(LifecycleEventType.REMOVED)
        .timestamp(Instant.parse("2026-05-19T16:40:03Z"))
        .build();

    assertFalse(handler.canHandle(event));
  }

  @Test
  void handleEvent_shouldDelegateToCreatedLifecycleServiceMethod() {
    RobotLifecycleEvent event = RobotLifecycleEvent.builder()
        .robotId("robot-1")
        .eventType(LifecycleEventType.CREATED)
        .timestamp(Instant.parse("2026-05-19T16:40:03Z"))
        .build();

    handler.handleEvent(event);

    verify(robotStateService).applyCreatedLifecycleEvent(event);
  }
}
