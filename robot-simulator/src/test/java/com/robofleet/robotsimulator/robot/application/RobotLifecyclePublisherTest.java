package com.robofleet.robotsimulator.robot.application;

import static org.mockito.Mockito.verify;

import com.robofleet.robotsimulator.robot.infrastructure.messaging.event.RobotLifecycleEvent;
import com.robofleet.robotsimulator.robot.infrastructure.messaging.event.RobotStateChangedEvent;
import java.lang.reflect.Field;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

@ExtendWith(MockitoExtension.class)
class RobotLifecyclePublisherTest {

  @Mock
  private KafkaTemplate<String, Object> kafkaTemplate;

  @InjectMocks
  private RobotLifecyclePublisher robotLifecyclePublisher;

  @BeforeEach
  void setUp() throws Exception {
    Field lifecycleTopicField = RobotLifecyclePublisher.class.getDeclaredField("lifecycleTopic");
    lifecycleTopicField.setAccessible(true);
    lifecycleTopicField.set(robotLifecyclePublisher, "robot.lifecycle");

    Field telemetryTopicField = RobotLifecyclePublisher.class.getDeclaredField("telemetryTopic");
    telemetryTopicField.setAccessible(true);
    telemetryTopicField.set(robotLifecyclePublisher, "robot.telemetry");
  }

  @Test
  void publishLifecycle_shouldSendLifecycleEventWithRobotIdAsKey() {
    RobotLifecycleEvent lifecycleEvent = RobotLifecycleEvent.builder()
        .robotId("robot-1")
        .displayName("Alpha")
        .positionX(10.0)
        .positionY(20.0)
        .battery(80.0)
        .status("MOVING")
        .eventType("REMOVED")
        .timestamp(Instant.now())
        .build();

    robotLifecyclePublisher.publishLifecycle(lifecycleEvent);

    verify(kafkaTemplate).send("robot.lifecycle", "robot-1", lifecycleEvent);
  }

  @Test
  void publishTelemetry_shouldSendTelemetryEventWithRobotIdAsKey() {
    RobotStateChangedEvent telemetryEvent = RobotStateChangedEvent.builder()
        .robotId("robot-1")
        .displayName(null)
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
