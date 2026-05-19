package com.robofleet.fleetstateservice.robot.application;

import com.robofleet.fleetstateservice.robot.application.dto.RobotStateResponse;
import com.robofleet.fleetstateservice.robot.infrastructure.messaging.RobotTelemetryEvent;
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
  void upsertFromTelemetry(RobotTelemetryEvent event);

  /**
   * Returns the current known state for every robot in the fleet snapshot.
   *
   * @param pageable paging and sorting request
   * @return list of latest robot states
   */
  List<RobotStateResponse> getAllRobots(Pageable pageable);

  /**
   * Returns the current known state for one robot, if available.
   *
   * @param robotId unique robot identifier
   * @return latest state wrapped in {@link Optional}
   */
  Optional<RobotStateResponse> getRobotById(String robotId);
}
