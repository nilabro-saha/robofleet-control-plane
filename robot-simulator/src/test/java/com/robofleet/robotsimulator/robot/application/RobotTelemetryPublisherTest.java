package com.robofleet.robotsimulator.robot.application;

import static org.mockito.Mockito.verify;

import com.robofleet.robotsimulator.robot.infrastructure.messaging.RobotTelemetryEvent;
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
class RobotTelemetryPublisherTest {

  @Mock
  private KafkaTemplate<String, RobotTelemetryEvent> kafkaTemplate;

  @InjectMocks
  private RobotTelemetryPublisher robotTelemetryPublisher;

  @BeforeEach
  void setUp() throws Exception {
    Field telemetryTopicField = RobotTelemetryPublisher.class.getDeclaredField("telemetryTopic");
    telemetryTopicField.setAccessible(true);
    telemetryTopicField.set(robotTelemetryPublisher, "robot.telemetry");
  }

  @Test
  void publish_shouldSendTelemetryWithRobotIdAsKey() {
    RobotTelemetryEvent telemetryEvent = RobotTelemetryEvent.builder()
        .robotId("robot-1")
        .positionX(10.0)
        .positionY(20.0)
        .battery(80.0)
        .status("MOVING")
        .timestamp(Instant.now())
        .build();

    robotTelemetryPublisher.publish(telemetryEvent);

    verify(kafkaTemplate).send("robot.telemetry", "robot-1", telemetryEvent);
  }
}
