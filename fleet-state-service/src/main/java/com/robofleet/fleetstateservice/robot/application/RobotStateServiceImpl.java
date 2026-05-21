package com.robofleet.fleetstateservice.robot.application;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.robofleet.fleetstateservice.robot.application.dto.CreateRobotRequest;
import com.robofleet.fleetstateservice.robot.application.dto.RobotCreationResponse;
import com.robofleet.fleetstateservice.robot.application.dto.RobotStateResponse;
import com.robofleet.fleetstateservice.robot.application.dto.RobotSummaryResponse;
import com.robofleet.fleetstateservice.robot.application.dto.SortableFieldMapping;
import com.robofleet.fleetstateservice.robot.domain.Robot;
import com.robofleet.fleetstateservice.robot.domain.RobotLifecycleStatus;
import com.robofleet.fleetstateservice.robot.domain.RobotState;
import com.robofleet.fleetstateservice.robot.infrastructure.messaging.LifecycleEventType;
import com.robofleet.fleetstateservice.robot.infrastructure.messaging.RobotLifecycleEvent;
import com.robofleet.fleetstateservice.robot.infrastructure.messaging.RobotStateChangedEvent;
import com.robofleet.fleetstateservice.robot.infrastructure.persistence.RobotRepository;
import com.robofleet.fleetstateservice.robot.infrastructure.persistence.RobotStateRepository;
import java.lang.reflect.Field;
import java.time.Instant;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

  private static final Map<String, String> ROBOT_STATUS_SORT_FIELD_MAPPING =
      createRobotStatusSortFieldMapping();

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
  public List<RobotStateResponse> getAllRobotStatuses(Pageable pageable) {
    return robotStateRepository.findAllRobotStatuses(mapRobotStatusPageable(pageable))
        .getContent();
  }

  /**
   * Retrieves latest-state row for one robot.
   *
   * @param robotId unique robot identifier
   * @return optional response if robot is known
   */
  @Override
  public Optional<RobotStateResponse> getRobotStatusById(String robotId) {
    return robotStateRepository.findRobotStatusByRobotId(robotId);
  }

  /**
   * Retrieves robot identity/lifecycle rows for all robots.
   */
  @Override
  public List<RobotSummaryResponse> getAllRobots(Pageable pageable) {
    return robotRepository.findAllRobotSummaries(pageable)
        .getContent();
  }

  /**
   * Retrieves one robot identity/lifecycle row by id.
   */
  @Override
  public Optional<RobotSummaryResponse> getRobotById(String robotId) {
    return robotRepository.findRobotSummaryByRobotId(robotId);
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

  private Pageable mapRobotStatusPageable(Pageable pageable) {
    Sort mappedSort = Sort.unsorted();

    for (Sort.Order order : pageable.getSort()) {
      String mappedProperty = ROBOT_STATUS_SORT_FIELD_MAPPING.getOrDefault(
          order.getProperty(),
          order.getProperty());
      mappedSort = mappedSort.and(Sort.by(new Sort.Order(order.getDirection(), mappedProperty)));
    }

    return PageRequest.of(
        pageable.getPageNumber(),
        pageable.getPageSize(),
        mappedSort
    );
  }

  private static Map<String, String> createRobotStatusSortFieldMapping() {
    return Arrays.stream(RobotStateResponse.class.getDeclaredFields())
        .filter(field -> field.isAnnotationPresent(SortableFieldMapping.class))
        .collect(Collectors.toMap(
            RobotStateServiceImpl::resolveEffectiveJsonFieldName,
            RobotStateServiceImpl::resolveAliasedEntityFieldName,
            (left, right) -> left,
            LinkedHashMap::new
        ));
  }

  private static String resolveEffectiveJsonFieldName(Field field) {
    JsonProperty jsonProperty = field.getAnnotation(JsonProperty.class);
    if (jsonProperty == null || jsonProperty.value().isBlank()) {
      return field.getName();
    }

    return jsonProperty.value();
  }

  private static String resolveAliasedEntityFieldName(Field field) {
    SortableFieldMapping sortableFieldMapping = field.getAnnotation(SortableFieldMapping.class);
    String entityField = sortableFieldMapping.entityField().isBlank()
        ? field.getName()
        : sortableFieldMapping.entityField();

    return sortableFieldMapping.entityAlias() + "." + entityField;
  }
}
