package com.robofleet.fleetstateservice.robot.infrastructure.messaging;

import com.robofleet.fleetstateservice.robot.application.RobotCreationCommandGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Publishes lifecycle commands to the shared robot lifecycle topic.
 *
 * <p>This is the infrastructure adapter for the
 * {@link com.robofleet.fleetstateservice.robot.application.RobotCreationCommandGateway}
 * outbound port.
 *
 * @author Nilabro Saha
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class RobotCreationCommandPublisher implements RobotCreationCommandGateway {

  private final KafkaTemplate<String, Object> kafkaTemplate;

  @Value("${fleet.state.kafka.lifecycle-topic:robot.lifecycle}")
  private String lifecycleTopic;

  /**
   * Publishes one lifecycle event keyed by robot id.
   *
   * @param command lifecycle command payload
   */
  @Override
  public void publishLifecycleEvent(RobotLifecycleEvent command) {
    kafkaTemplate.send(lifecycleTopic, command.getRobotId(), command);
    log.info("Published lifecycle {} for robotId={}", command.getEventType(), command.getRobotId());
  }
}
