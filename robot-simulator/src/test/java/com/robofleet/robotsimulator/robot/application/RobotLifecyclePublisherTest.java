package com.robofleet.robotsimulator.robot.application;

import static org.mockito.Mockito.verify;

import com.robofleet.robotsimulator.robot.infrastructure.messaging.event.RobotLifecycleEvent;
import com.robofleet.robotsimulator.robot.infrastructure.messaging.event.LifecycleEventType;
import com.robofleet.robotsimulator.robot.infrastructure.messaging.event.RobotStateChangedEvent;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class RobotLifecyclePublisherTest {

  @Mock
  private KafkaTemplate<String, Object> kafkaTemplate;

  @InjectMocks
  private RobotLifecyclePublisher robotLifecyclePublisher;

  @BeforeEach
  void setUp() {
    ReflectionTestUtils.setField(robotLifecyclePublisher, "lifecycleTopic", "robot.lifecycle");
    ReflectionTestUtils.setField(robotLifecyclePublisher, "telemetryTopic", "robot.telemetry");
  }

  @Test
  void publishLifecycle_shouldSendLifecycleEventWithRobotIdAsKey() {
    RobotLifecycleEvent lifecycleEvent = RobotLifecycleEvent.builder()
        .eventName("robot-lifecycle-changed")
        .correlationId("corr-1")
        .robotId("robot-1")
        .positionX(10.0)
        .positionY(20.0)
        .battery(80.0)
        .status("MOVING")
        .eventType(LifecycleEventType.REMOVED)
        .timestamp(Instant.now())
        .build();

    robotLifecyclePublisher.publishLifecycle(lifecycleEvent);

    verify(kafkaTemplate).send("robot.lifecycle", "robot-1", lifecycleEvent);
  }

  @Test
  void publishTelemetry_shouldSendTelemetryEventWithRobotIdAsKey() {
    RobotStateChangedEvent telemetryEvent = RobotStateChangedEvent.builder()
        .eventName("robot-state-changed")
        .correlationId("corr-2")
        .robotId("robot-1")
        .positionX(10.0)
        .positionY(20.0)
        .battery(80.0)
        .status("MOVING")
        .timestamp(Instant.now())
        .build();

    robotLifecyclePublisher.publishTelemetry(telemetryEvent);

    verify(kafkaTemplate).send("robot.telemetry", "robot-1", telemetryEvent);
  }
}
