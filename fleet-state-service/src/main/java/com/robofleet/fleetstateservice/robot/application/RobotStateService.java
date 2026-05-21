package com.robofleet.fleetstateservice.robot.application;

import com.robofleet.fleetstateservice.robot.application.dto.CreateRobotRequest;
import com.robofleet.fleetstateservice.robot.application.dto.RobotCreationResponse;
import com.robofleet.fleetstateservice.robot.application.dto.RobotStateResponse;
import com.robofleet.fleetstateservice.robot.application.dto.RobotSummaryResponse;
import com.robofleet.fleetstateservice.robot.infrastructure.messaging.RobotLifecycleEvent;
import com.robofleet.fleetstateservice.robot.infrastructure.messaging.RobotStateChangedEvent;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;

/**
 * Application boundary for fleet state use cases.
 *
 * <p>Intent: keep higher-level behavior explicit (ingest telemetry, read latest state)
 * while isolating callers from persistence and transport concerns.</p>
 */
public interface RobotStateService {

  /**
   * Ingests one telemetry event and updates the latest-state projection.
   *
   * @param event telemetry payload emitted by a robot/simulator
   */
  void upsertFromTelemetry(RobotStateChangedEvent event);

  /**
   * Creates a robot orchestration request and marks robot as pending.
   *
   * @param request api payload for robot creation
   * @return pending creation response
   */
  RobotCreationResponse createRobot(CreateRobotRequest request);

  /**
   * Marks a robot as pending deletion and publishes a delete lifecycle command.
   *
   * @param robotId unique robot identifier
   * @return updated robot summary when robot exists
   */
  Optional<RobotSummaryResponse> requestRobotDeletion(String robotId);

  /**
   * Returns the current known state for every robot in the fleet snapshot.
   *
   * @param pageable paging and sorting request
   * @return list of latest robot states
   */
  List<RobotStateResponse> getAllRobotStatuses(Pageable pageable);

  /**
   * Returns the current known state for one robot, if available.
   *
   * @param robotId unique robot identifier
   * @return latest state wrapped in {@link Optional}
   */
  Optional<RobotStateResponse> getRobotStatusById(String robotId);

  /**
   * Returns robot identity/lifecycle projection for all robots.
   *
   * @param pageable paging and sorting request
   * @return list of robot identity/lifecycle summaries
   */
  List<RobotSummaryResponse> getAllRobots(Pageable pageable);

  /**
   * Returns robot identity/lifecycle projection for one robot, if available.
   *
   * @param robotId unique robot identifier
   * @return robot summary wrapped in {@link Optional}
   */
  Optional<RobotSummaryResponse> getRobotById(String robotId);

  /**
   * Removes one robot from the latest-state projection.
   *
   * @param robotId unique robot identifier
   */
  void removeRobotById(String robotId);

  /**
   * Applies CREATED lifecycle data to materialized state.
   *
   * @param event created lifecycle event payload
   */
  void applyCreatedLifecycleEvent(RobotLifecycleEvent event);

  /**
   * Applies REMOVED lifecycle data to materialized state.
   *
   * @param event removed lifecycle event payload
   */
  void applyRemovedLifecycleEvent(RobotLifecycleEvent event);
}
