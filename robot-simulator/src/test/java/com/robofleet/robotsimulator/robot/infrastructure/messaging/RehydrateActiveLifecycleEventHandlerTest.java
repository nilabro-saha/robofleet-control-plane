package com.robofleet.robotsimulator.robot.infrastructure.messaging;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;

import com.robofleet.robotsimulator.robot.application.RobotOrchestrator;
import com.robofleet.robotsimulator.robot.application.actor.RobotOrchestration;
import com.robofleet.robotsimulator.robot.domain.model.RobotStatus;
import com.robofleet.robotsimulator.robot.infrastructure.messaging.event.LifecycleEventType;
import com.robofleet.robotsimulator.robot.infrastructure.messaging.event.RobotLifecycleEvent;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RehydrateActiveLifecycleEventHandlerTest {

  @Mock
  private RobotOrchestrator robotOrchestrator;

  @InjectMocks
  private RehydrateActiveLifecycleEventHandler handler;

  @Test
  void canHandle_shouldReturnTrueForRehydrateActive() {
    RobotLifecycleEvent event = RobotLifecycleEvent.builder()
        .robotId("robot-1")
        .eventType(LifecycleEventType.REHYDRATE_ACTIVE)
        .timestamp(Instant.parse("2026-05-20T10:00:00Z"))
        .build();

    assertTrue(handler.canHandle(event));
  }

  @Test
  void canHandle_shouldReturnFalseForOtherLifecycleTypes() {
    RobotLifecycleEvent event = RobotLifecycleEvent.builder()
        .robotId("robot-1")
        .eventType(LifecycleEventType.CREATE_PENDING)
        .timestamp(Instant.parse("2026-05-20T10:00:00Z"))
        .build();

    assertFalse(handler.canHandle(event));
  }

  @Test
  void handleEvent_shouldSendFixedStateSpawnCommandToRegistry() {
    RobotLifecycleEvent event = RobotLifecycleEvent.builder()
        .robotId("robot-24")
        .positionX(11.5)
        .positionY(7.25)
        .battery(76.4)
        .status("CHARGING")
        .eventType(LifecycleEventType.REHYDRATE_ACTIVE)
        .timestamp(Instant.parse("2026-05-20T10:00:00Z"))
        .build();

    handler.handleEvent(event);

    ArgumentCaptor<RobotOrchestration.Spawn> commandCaptor =
        ArgumentCaptor.forClass(RobotOrchestration.Spawn.class);
    verify(robotOrchestrator).tell(commandCaptor.capture());

    var requestedState = commandCaptor.getValue().requestedState();
    assertTrue("robot-24".equals(requestedState.robotId()));
    assertTrue(requestedState.positionX() == 11.5);
    assertTrue(requestedState.positionY() == 7.25);
    assertTrue(requestedState.battery() == 76.4);
    assertTrue(requestedState.status() == RobotStatus.CHARGING);
  }
}