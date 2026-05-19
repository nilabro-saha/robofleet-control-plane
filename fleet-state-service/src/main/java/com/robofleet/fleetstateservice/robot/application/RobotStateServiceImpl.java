package com.robofleet.fleetstateservice.robot.application;

import com.robofleet.fleetstateservice.robot.application.dto.RobotStateResponse;
import com.robofleet.fleetstateservice.robot.domain.RobotState;
import com.robofleet.fleetstateservice.robot.infrastructure.messaging.RobotTelemetryEvent;
import com.robofleet.fleetstateservice.robot.infrastructure.persistence.RobotStateRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
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

  private final RobotStateRepository robotStateRepository;

  /**
   * Upserts the latest state row for the telemetry's robot id.
   *
   * @param event telemetry payload to materialize
   */
  @Override
  public void upsertFromTelemetry(RobotTelemetryEvent event) {
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
   * Retrieves latest-state rows for all robots.
   *
   * @return list of API response objects
   */
  @Override
  public List<RobotStateResponse> getAllRobots() {
    return robotStateRepository.findAll()
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
   * Maps persistence entity into API response shape.
   *
   * @param entity persisted latest-state row
   * @return response DTO for API consumers
   */
  private RobotStateResponse toResponse(RobotState entity) {
    return RobotStateResponse.builder()
        .robotId(entity.getRobotId())
        .positionX(entity.getPositionX())
        .positionY(entity.getPositionY())
        .battery(entity.getBattery())
        .status(entity.getStatus())
        .timestamp(entity.getTimestamp())
        .build();
  }
}
