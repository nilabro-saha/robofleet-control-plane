package com.robofleet.fleetstateservice.robot.infrastructure.messaging;

import com.robofleet.fleetstateservice.robot.application.RobotCreationCommandGateway;
import com.robofleet.fleetstateservice.robot.domain.RobotLifecycleStatus;
import com.robofleet.fleetstateservice.robot.domain.RobotState;
import com.robofleet.fleetstateservice.robot.infrastructure.persistence.RobotRepository;
import com.robofleet.fleetstateservice.robot.infrastructure.persistence.RobotStateRepository;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Publishes startup rehydration commands for already ACTIVE robots.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
    value = "fleet.state.rehydration.publish-on-startup-enabled",
    havingValue = "true",
    matchIfMissing = true
)
public class ActiveRobotRehydrationPublisher {

  private final RobotRepository robotRepository;
  private final RobotStateRepository robotStateRepository;
  private final RobotCreationCommandGateway robotCreationCommandGateway;

  /**
   * On service startup, emit one REHYDRATE_ACTIVE event for each active robot.
   */
  @EventListener(ApplicationReadyEvent.class)
  public void publishRehydrationCommands() {
    var activeRobots = robotRepository.findByLifecycleStatus(RobotLifecycleStatus.ACTIVE);

    for (var robot : activeRobots) {
      try {
        var lastState = robotStateRepository.findById(robot.getRobotId());

        robotCreationCommandGateway.publishLifecycleEvent(
            RobotLifecycleEvent.builder()
                .robotId(robot.getRobotId())
                .positionX(lastState.map(RobotState::getPositionX).orElse(null))
                .positionY(lastState.map(RobotState::getPositionY).orElse(null))
                .battery(lastState.map(RobotState::getBattery).orElse(null))
                .status(lastState.map(RobotState::getStatus).orElse(null))
                .eventType(LifecycleEventType.REHYDRATE_ACTIVE)
                .timestamp(lastState.map(RobotState::getTimestamp).orElse(Instant.now()))
                .build()
        );
      } catch (Exception exception) {
        log.error("Failed to publish rehydration event for robotId={}", robot.getRobotId(),
            exception);
      }
    }

    log.info("Published {} startup rehydration events for active robots", activeRobots.size());
  }
}
