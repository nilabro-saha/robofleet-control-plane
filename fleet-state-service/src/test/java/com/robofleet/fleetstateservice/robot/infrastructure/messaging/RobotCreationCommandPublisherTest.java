package com.robofleet.fleetstateservice.robot.infrastructure.messaging;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;

import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.test.util.ReflectionTestUtils;

class RobotCreationCommandPublisherTest {

  private CapturingKafkaTemplate kafkaTemplate;
  private RobotCreationCommandPublisher publisher;

  @BeforeEach
  void setUp() throws Exception {
    @SuppressWarnings("unchecked")
    ProducerFactory<String, Object> producerFactory = mock(ProducerFactory.class);
    kafkaTemplate = new CapturingKafkaTemplate(producerFactory);
    publisher = new RobotCreationCommandPublisher(kafkaTemplate);

    ReflectionTestUtils.setField(publisher, "lifecycleTopic", "robot.lifecycle");
  }

  @Test
  void publishLifecycleEvent_shouldSendToLifecycleTopicWithRobotIdKey() {
    RobotLifecycleEvent command = RobotLifecycleEvent.builder()
        .eventName("robot-lifecycle-changed")
        .correlationId("corr-1")
        .robotId("robot-1")
        .eventType(LifecycleEventType.CREATE_PENDING)
        .timestamp(Instant.parse("2026-05-19T16:40:03Z"))
        .build();

    publisher.publishLifecycleEvent(command);

    assertEquals("robot.lifecycle", kafkaTemplate.topic);
    assertEquals("robot-1", kafkaTemplate.key);
    assertSame(command, kafkaTemplate.value);
  }

  @Test
  void publishLifecycleEvent_shouldSupportRehydrateActiveType() {
    RobotLifecycleEvent command = RobotLifecycleEvent.builder()
        .eventName("robot-lifecycle-changed")
        .correlationId("corr-2")
        .robotId("robot-2")
        .eventType(LifecycleEventType.REHYDRATE_ACTIVE)
        .timestamp(Instant.parse("2026-05-19T16:50:03Z"))
        .build();

    publisher.publishLifecycleEvent(command);

    assertEquals("robot.lifecycle", kafkaTemplate.topic);
    assertEquals("robot-2", kafkaTemplate.key);
    assertSame(command, kafkaTemplate.value);
  }

  @Test
  void publishLifecycleEvent_shouldSupportDeletePendingType() {
    RobotLifecycleEvent command = RobotLifecycleEvent.builder()
        .eventName("robot-lifecycle-changed")
        .correlationId("corr-3")
        .robotId("robot-3")
        .eventType(LifecycleEventType.DELETE_PENDING)
        .timestamp(Instant.parse("2026-05-19T16:55:03Z"))
        .build();

    publisher.publishLifecycleEvent(command);

    assertEquals("robot.lifecycle", kafkaTemplate.topic);
    assertEquals("robot-3", kafkaTemplate.key);
    assertSame(command, kafkaTemplate.value);
  }

  private static final class CapturingKafkaTemplate extends KafkaTemplate<String, Object> {

    private String topic;
    private String key;
    private Object value;

    private CapturingKafkaTemplate(ProducerFactory<String, Object> producerFactory) {
      super(producerFactory);
    }

    @Override
    public CompletableFuture<SendResult<String, Object>> send(String topic, String key, Object data) {
      this.topic = topic;
      this.key = key;
      this.value = data;
      return CompletableFuture.completedFuture(
          new SendResult<>(new ProducerRecord<>(topic, key, data), null));
    }
  }
}
