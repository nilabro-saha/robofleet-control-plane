package com.robofleet.fleetstateservice.robot.application;

import com.robofleet.fleetstateservice.robot.application.dto.CreateRobotRequest;
import com.robofleet.fleetstateservice.robot.application.dto.RobotCreationResponse;
import com.robofleet.fleetstateservice.robot.application.dto.RobotStateResponse;
import com.robofleet.fleetstateservice.robot.domain.Robot;
import com.robofleet.fleetstateservice.robot.domain.RobotLifecycleStatus;
import com.robofleet.fleetstateservice.robot.domain.RobotState;
import com.robofleet.fleetstateservice.robot.infrastructure.messaging.LifecycleEventType;
import com.robofleet.fleetstateservice.robot.infrastructure.messaging.RobotLifecycleEvent;
import com.robofleet.fleetstateservice.robot.infrastructure.messaging.RobotStateChangedEvent;
import com.robofleet.fleetstateservice.robot.infrastructure.persistence.RobotRepository;
import com.robofleet.fleetstateservice.robot.infrastructure.persistence.RobotStateRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/**
 * Default implementation of fleet state behavior.
 *
 * <p>Intent: maintain an always-current snapshot by upserting incoming telemetry
 * and serving read-optimized DTOs for API consumers.</p>
 */
@Service
@RequiredArgsConstructor
public class RobotStateServiceImpl implements RobotStateService {

  private final RobotRepository robotRepository;
  private final RobotStateRepository robotStateRepository;
  private final RobotCreationCommandGateway robotCreationCommandGateway;

  /**
   * Upserts the latest state row for the telemetry's robot id.
   *
   * @param event telemetry payload to materialize
   */
  @Override
  public void upsertFromTelemetry(RobotStateChangedEvent event) {
    Optional<Robot> existingRobot = robotRepository.findById(event.getRobotId());
    String normalizedDisplayName = existingRobot
        .map(Robot::getDisplayName)
        .orElse(null);

    RobotLifecycleStatus lifecycleStatus = existingRobot
        .map(Robot::getLifecycleStatus)
        .orElse(RobotLifecycleStatus.ACTIVE);

    Robot robot = Robot.builder()
        .robotId(event.getRobotId())
        .displayName(normalizedDisplayName)
        .lifecycleStatus(lifecycleStatus)
        .build();
    robotRepository.save(robot);

    RobotState entity = RobotState.builder()
        .robotId(event.getRobotId())
        .positionX(event.getPositionX())
        .positionY(event.getPositionY())
        .battery(event.getBattery())
        .status(event.getStatus())
        .timestamp(event.getTimestamp())
        .build();

    robotStateRepository.save(entity);
  }

  /**
   * Creates a robot orchestration request and persists pending status.
   */
  @Override
  public RobotCreationResponse createRobot(CreateRobotRequest request) {
    String generatedRobotId = UUID.randomUUID().toString();

    Robot pendingRobot = Robot.builder()
        .robotId(generatedRobotId)
        .displayName(request.getDisplayName())
        .lifecycleStatus(RobotLifecycleStatus.CREATE_PENDING)
        .build();
    robotRepository.save(pendingRobot);

    robotCreationCommandGateway.publishLifecycleEvent(
        RobotLifecycleEvent.builder()
            .robotId(generatedRobotId)
            .eventType(LifecycleEventType.CREATE_PENDING)
            .timestamp(Instant.now())
            .build()
    );

    return new RobotCreationResponse(
        generatedRobotId,
        request.getDisplayName(),
        RobotLifecycleStatus.CREATE_PENDING.name()
    );
  }

  /**
   * Retrieves latest-state rows for all robots.
   *
   * @param pageable pagination and sorting request
   * @return list of API response objects
   */
  @Override
  public List<RobotStateResponse> getAllRobots(Pageable pageable) {
    return robotStateRepository.findAll(pageable)
        .getContent()
        .stream()
        .map(this::toResponse)
        .toList();
  }

  /**
   * Retrieves latest-state row for one robot.
   *
   * @param robotId unique robot identifier
   * @return optional response if robot is known
   */
  @Override
  public Optional<RobotStateResponse> getRobotById(String robotId) {
    return robotStateRepository.findById(robotId).map(this::toResponse);
  }

  /**
   * Removes latest-state row for one robot.
   *
   * @param robotId unique robot identifier
   */
  @Override
  public void removeRobotById(String robotId) {
    robotStateRepository.deleteById(robotId);
    robotRepository.deleteById(robotId);
  }

  /**
   * Applies REMOVED lifecycle update by deleting robot projections.
   */
  @Override
  public void applyRemovedLifecycleEvent(RobotLifecycleEvent event) {
    removeRobotById(event.getRobotId());
  }

  /**
   * Applies CREATED lifecycle update by activating and seeding robot state.
   */
  @Override
  public void applyCreatedLifecycleEvent(RobotLifecycleEvent event) {
    robotRepository.findById(event.getRobotId())
        .ifPresent(robot -> {
          robot.markAsActive();
          robotRepository.save(robot);

          Optional<RobotState> existingState = robotStateRepository.findById(event.getRobotId());
          RobotState updatedState = RobotState.builder()
              .robotId(event.getRobotId())
              .positionX(event.getPositionX() == null
                  ? existingState.map(RobotState::getPositionX).orElse(0.0)
                  : event.getPositionX())
              .positionY(event.getPositionY() == null
                  ? existingState.map(RobotState::getPositionY).orElse(0.0)
                  : event.getPositionY())
              .battery(event.getBattery() == null
                  ? existingState.map(RobotState::getBattery).orElse(0.0)
                  : event.getBattery())
              .status(event.getStatus() == null
                  ? existingState.map(RobotState::getStatus).orElse("UNKNOWN")
                  : event.getStatus())
              .timestamp(event.getTimestamp() == null
                  ? existingState.map(RobotState::getTimestamp).orElse(Instant.now())
                  : event.getTimestamp())
              .build();
          robotStateRepository.save(updatedState);
        });
  }

  /**
   * Maps persistence entity into API response shape.
   *
   * @param entity persisted latest-state row
   * @return response DTO for API consumers
   */
  private RobotStateResponse toResponse(RobotState entity) {
    String displayName = robotRepository.findById(entity.getRobotId())
        .map(Robot::getDisplayName)
        .orElse(null);

    return RobotStateResponse.builder()
        .robotId(entity.getRobotId())
        .displayName(displayName)
        .positionX(entity.getPositionX())
        .positionY(entity.getPositionY())
        .battery(entity.getBattery())
        .status(entity.getStatus())
        .timestamp(entity.getTimestamp())
        .build();
  }
}
