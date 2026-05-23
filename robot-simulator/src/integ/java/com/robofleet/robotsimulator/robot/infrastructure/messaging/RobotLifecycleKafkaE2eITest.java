package com.robofleet.robotsimulator.robot.infrastructure.messaging;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.robofleet.robotsimulator.robot.infrastructure.messaging.event.LifecycleEventType;
import com.robofleet.robotsimulator.robot.infrastructure.messaging.event.RobotLifecycleEvent;
import com.robofleet.robotsimulator.testsupport.KafkaEventObserver;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("itest")
@EmbeddedKafka(partitions = 1, topics = {"robot.lifecycle"})
@DirtiesContext
class RobotLifecycleKafkaE2eITest {

  private static final String LIFECYCLE_TOPIC = "robot.lifecycle";

  @Autowired
  private KafkaTemplate<String, Object> kafkaTemplate;

  @Autowired
  private KafkaEventObserver kafkaEventObserver;

  @Test
  void shouldConsumeCreatePendingAndEmitCreatedWithSameCorrelationId() {
    String robotId = "itest-robot-" + UUID.randomUUID();
    String correlationId = "corr-e2e-" + UUID.randomUUID();

    RobotLifecycleEvent incomingCreatePending = incomingLifecycleEvent(
        robotId,
        correlationId,
        LifecycleEventType.CREATE_PENDING
    );

    kafkaTemplate.send(LIFECYCLE_TOPIC, robotId, incomingCreatePending);
    kafkaTemplate.flush();

    await()
        .atMost(Duration.ofSeconds(20))
        .untilAsserted(() -> {
          Optional<RobotLifecycleEvent> createdEvent = kafkaEventObserver.findMatchingEvent(
              RobotLifecycleEvent.class,
              event -> LifecycleEventType.CREATED == event.getEventType()
                  && robotId.equals(event.getRobotId())
                  && correlationId.equals(event.getCorrelationId())
          );

          assertNotNull(createdEvent.orElse(null));
        });
  }

  @Test
  void shouldConsumeRehydrateActiveAndEmitCreatedWithSameCorrelationId() {
    String robotId = "itest-robot-" + UUID.randomUUID();
    String correlationId = "corr-e2e-" + UUID.randomUUID();

    RobotLifecycleEvent incomingRehydrateActive = RobotLifecycleEvent.builder()
        .eventName("robot-lifecycle-changed")
        .correlationId(correlationId)
        .robotId(robotId)
        .positionX(12.5)
        .positionY(7.25)
        .battery(88.0)
        .status("IDLE")
        .eventType(LifecycleEventType.REHYDRATE_ACTIVE)
        .timestamp(Instant.now())
        .build();

    kafkaTemplate.send(LIFECYCLE_TOPIC, robotId, incomingRehydrateActive);
    kafkaTemplate.flush();

    await()
        .atMost(Duration.ofSeconds(20))
        .untilAsserted(() -> {
          Optional<RobotLifecycleEvent> createdEvent = kafkaEventObserver.findMatchingEvent(
              RobotLifecycleEvent.class,
              event -> LifecycleEventType.CREATED == event.getEventType()
                  && robotId.equals(event.getRobotId())
                  && correlationId.equals(event.getCorrelationId())
          );

          assertNotNull(createdEvent.orElse(null));
        });
  }

  @Test
  void shouldConsumeDeletePendingAndEmitRemovedWithSameCorrelationId() {
    String robotId = "itest-robot-" + UUID.randomUUID();
    String createCorrelationId = "corr-create-" + UUID.randomUUID();
    String deleteCorrelationId = "corr-delete-" + UUID.randomUUID();

    kafkaTemplate.send(
        LIFECYCLE_TOPIC,
        robotId,
        incomingLifecycleEvent(robotId, createCorrelationId, LifecycleEventType.CREATE_PENDING)
    );
    kafkaTemplate.flush();

    await()
        .atMost(Duration.ofSeconds(20))
        .untilAsserted(() -> {
          Optional<RobotLifecycleEvent> createdEvent = kafkaEventObserver.findMatchingEvent(
              RobotLifecycleEvent.class,
              event -> LifecycleEventType.CREATED == event.getEventType()
                  && robotId.equals(event.getRobotId())
                  && createCorrelationId.equals(event.getCorrelationId())
          );

          assertNotNull(createdEvent.orElse(null));
        });

    kafkaTemplate.send(
        LIFECYCLE_TOPIC,
        robotId,
        incomingLifecycleEvent(robotId, deleteCorrelationId, LifecycleEventType.DELETE_PENDING)
    );
    kafkaTemplate.flush();

    await()
        .atMost(Duration.ofSeconds(20))
        .untilAsserted(() -> {
          Optional<RobotLifecycleEvent> removedEvent = kafkaEventObserver.findMatchingEvent(
              RobotLifecycleEvent.class,
              event -> LifecycleEventType.REMOVED == event.getEventType()
                  && robotId.equals(event.getRobotId())
                  && deleteCorrelationId.equals(event.getCorrelationId())
          );

          assertNotNull(removedEvent.orElse(null));
        });
  }

  private RobotLifecycleEvent incomingLifecycleEvent(
      String robotId,
      String correlationId,
      LifecycleEventType eventType
  ) {
    return RobotLifecycleEvent.builder()
        .eventName("robot-lifecycle-changed")
        .correlationId(correlationId)
        .robotId(robotId)
        .eventType(eventType)
        .timestamp(Instant.now())
        .build();
  }
}
