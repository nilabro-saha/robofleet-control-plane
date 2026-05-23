package com.robofleet.robotsimulator.testsupport;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

/**
 * Reusable integration-test Kafka observer that stores consumed events by topic.
 *
 * <p>Intent: provide one extensible observer where additional topic listener methods can be added
 * over time while tests query events through a common matching API.</p>
 *
 * @author Nilabro Saha
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaEventObserver {

  private final ObjectMapper objectMapper;

  private final Map<String, List<JsonNode>> eventsByTopic = new ConcurrentHashMap<>();

  /**
   * Observes lifecycle-topic events and stores them in the topic-indexed event store.
   *
   * @param event consumed lifecycle event payload
   * @param topic received topic name
   */
  @KafkaListener(
      topics = "${robot.simulator.kafka.lifecycle-topic:robot.lifecycle}",
      groupId = "${robot.simulator.kafka.observer-group-id:robot-simulator-itest-observer}"
  )
  public void onMessageReceived(
      Object event,
      @Header(KafkaHeaders.RECEIVED_TOPIC) String topic
  ) {
    Object payload = event;
    if (event instanceof ConsumerRecord<?, ?> consumerRecord) {
      payload = consumerRecord.value();
    }

    JsonNode normalizedEvent = objectMapper.valueToTree(payload);
    eventsByTopic.computeIfAbsent(topic, ignored -> new CopyOnWriteArrayList<>())
        .add(normalizedEvent);
  }

  /**
   * Finds the first consumed event matching expected shape and predicate across all observed topics.
   *
   * @param expectedType expected payload type/class
   * @param predicate attribute matcher used for assertions
   * @param <T> expected event type
   * @return optional matching event
   */
  public <T> Optional<T> findMatchingEvent(Class<T> expectedType, Predicate<T> predicate) {
    for (Map.Entry<String, List<JsonNode>> entry : eventsByTopic.entrySet()) {
      String topic = entry.getKey();
      for (JsonNode rawEvent : entry.getValue()) {
        final T mappedEvent;
        try {
          mappedEvent = objectMapper.treeToValue(rawEvent, expectedType);
        } catch (Exception e) {
          log.warn("Failed to map Kafka observed event to expected type {} for topic {}",
              expectedType.getName(), topic, e);
          continue;
        }

        if (predicate.test(mappedEvent)) {
          return Optional.of(mappedEvent);
        }
      }
    }
    return Optional.empty();
  }
}
