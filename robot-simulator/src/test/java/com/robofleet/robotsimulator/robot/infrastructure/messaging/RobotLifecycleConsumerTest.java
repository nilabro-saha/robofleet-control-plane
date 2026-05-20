package com.robofleet.robotsimulator.robot.infrastructure.messaging;

import com.robofleet.robotsimulator.robot.infrastructure.messaging.event.RobotLifecycleEvent;
import com.robofleet.robotsimulator.robot.infrastructure.messaging.event.LifecycleEventType;
import java.time.Instant;
import java.util.List;
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
  private RobotLifecycleEventHandler matchingHandler;

  @Mock
  private RobotLifecycleEventHandler nonMatchingHandler;

  @InjectMocks
  private RobotLifecycleConsumer consumer;

  @org.junit.jupiter.api.BeforeEach
  void setUp() {
    consumer = new RobotLifecycleConsumer(List.of(matchingHandler, nonMatchingHandler));
  }

  @Test
  void shouldDispatchToMatchingHandler() {
    RobotLifecycleEvent command = RobotLifecycleEvent.builder()
        .robotId("robot-99")
        .eventType(LifecycleEventType.CREATE_PENDING)
        .timestamp(Instant.parse("2026-05-20T10:00:00Z"))
        .build();
    when(matchingHandler.canHandle(command)).thenReturn(true);
    when(nonMatchingHandler.canHandle(command)).thenReturn(false);

    consumer.consume(command);

    verify(matchingHandler).handleEvent(command);
    verify(nonMatchingHandler, never()).handleEvent(command);
  }

  @Test
  void shouldSwallowErrorsFromHandler() {
    RobotLifecycleEvent command = RobotLifecycleEvent.builder()
        .robotId("robot-99")
        .eventType(LifecycleEventType.CREATE_PENDING)
        .timestamp(Instant.parse("2026-05-20T10:00:00Z"))
        .build();
    when(matchingHandler.canHandle(command)).thenReturn(true);
    doThrow(new RuntimeException("boom"))
        .when(matchingHandler)
        .handleEvent(command);

    consumer.consume(command);

    verify(matchingHandler).handleEvent(command);
  }

  @Test
  void shouldIgnoreWhenNoHandlersMatch() {
    RobotLifecycleEvent command = RobotLifecycleEvent.builder()
        .robotId("robot-99")
        .eventType(LifecycleEventType.DELETE_PENDING)
        .timestamp(Instant.parse("2026-05-20T10:00:00Z"))
        .build();
    when(matchingHandler.canHandle(command)).thenReturn(false);
    when(nonMatchingHandler.canHandle(command)).thenReturn(false);

    consumer.consume(command);

    verify(matchingHandler, never()).handleEvent(command);
    verify(nonMatchingHandler, never()).handleEvent(command);
  }
}
