package com.robofleet.fleetstateservice.robot.infrastructure.persistence;

import com.robofleet.fleetstateservice.robot.application.dto.RobotStateResponse;
import com.robofleet.fleetstateservice.robot.domain.RobotState;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Persistence access point for current fleet snapshot data.
 *
 * <p>Intent: rely on Spring Data defaults for MVP simplicity; custom query
 * complexity is intentionally deferred until needed.</p>
 *
 * @author Nilabro Saha
 */
public interface RobotStateRepository extends JpaRepository<RobotState, String> {

  /**
   * Returns pageable latest-state rows for fleet snapshot queries.
   *
   * @param pageable pagination and sorting request
   * @return page of robot latest-state entities
   */
  Page<RobotState> findAll(Pageable pageable);

  /**
   * Returns projected robot statuses by joining latest state and robot identity.
   */
  @Query("""
      select new com.robofleet.fleetstateservice.robot.application.dto.RobotStateResponse(
        robot.robotId,
        robot.displayName,
        state.positionX,
        state.positionY,
        state.battery,
        robot.lifecycleStatus,
        state.status,
        state.timestamp
      )
      from Robot robot
      left join RobotState state on state.robotId = robot.robotId
      """)
  Page<RobotStateResponse> findAllRobotStatuses(Pageable pageable);

  /**
   * Returns projected robot status for one robot id.
   */
  @Query("""
      select new com.robofleet.fleetstateservice.robot.application.dto.RobotStateResponse(
        robot.robotId,
        robot.displayName,
        state.positionX,
        state.positionY,
        state.battery,
        robot.lifecycleStatus,
        state.status,
        state.timestamp
      )
      from Robot robot
      left join RobotState state on state.robotId = robot.robotId
      where robot.robotId = :robotId
      """)
  Optional<RobotStateResponse> findRobotStatusByRobotId(@Param("robotId") String robotId);
}
