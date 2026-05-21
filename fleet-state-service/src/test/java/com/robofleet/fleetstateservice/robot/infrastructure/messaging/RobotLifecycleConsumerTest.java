package com.robofleet.fleetstateservice.robot.infrastructure.messaging;

import com.robofleet.fleetstateservice.robot.application.RobotStateService;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RobotLifecycleConsumerTest {

  @Mock
  private RobotStateService robotStateService;

  @InjectMocks
  private RobotLifecycleConsumer consumer;

  @Mock
  private RobotLifecycleEventHandler matchingLifecycleHandler;

  @Mock
  private RobotLifecycleEventHandler nonMatchingLifecycleHandler;

  @BeforeEach
  void setUp() {
    consumer = new RobotLifecycleConsumer(
        robotStateService,
        List.of(matchingLifecycleHandler, nonMatchingLifecycleHandler)
    );
  }

  @Test
  void shouldDelegateConsumedEventToService() {
    RobotStateChangedEvent event = RobotStateChangedEvent.builder()
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
    RobotStateChangedEvent event = RobotStateChangedEvent.builder()
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

  @Test
  void shouldDispatchLifecycleToMatchingHandler() {
    RobotLifecycleEvent event = RobotLifecycleEvent.builder()
        .robotId("robot-1")
        .positionX(12.34)
        .positionY(56.78)
        .battery(87.1)
        .status("MOVING")
        .eventType(LifecycleEventType.REMOVED)
        .timestamp(Instant.parse("2026-05-19T16:40:03Z"))
        .build();
    when(matchingLifecycleHandler.canHandle(event)).thenReturn(true);
    when(nonMatchingLifecycleHandler.canHandle(event)).thenReturn(false);

    consumer.consumeLifecycle(event);

    verify(matchingLifecycleHandler).handleEvent(event);
    verify(nonMatchingLifecycleHandler, never()).handleEvent(event);
  }

  @Test
  void shouldIgnoreWhenNoLifecycleHandlersMatch() {
    RobotLifecycleEvent event = RobotLifecycleEvent.builder()
        .robotId("robot-1")
        .positionX(12.34)
        .positionY(56.78)
        .battery(87.1)
        .status("MOVING")
        .eventType(LifecycleEventType.CREATED)
        .timestamp(Instant.parse("2026-05-19T16:40:03Z"))
        .build();
    when(matchingLifecycleHandler.canHandle(event)).thenReturn(false);
    when(nonMatchingLifecycleHandler.canHandle(event)).thenReturn(false);

    consumer.consumeLifecycle(event);

    verify(matchingLifecycleHandler, never()).handleEvent(event);
    verify(nonMatchingLifecycleHandler, never()).handleEvent(event);
  }
}
