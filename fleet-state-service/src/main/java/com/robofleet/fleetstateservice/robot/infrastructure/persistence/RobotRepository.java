package com.robofleet.fleetstateservice.robot.infrastructure.persistence;

import com.robofleet.fleetstateservice.robot.application.dto.RobotSummaryResponse;
import com.robofleet.fleetstateservice.robot.domain.Robot;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Persistence access point for master robot identity rows.
 */
public interface RobotRepository extends JpaRepository<Robot, String> {

  /**
   * Returns projected robots identity + lifecycle information.
   */
  @Query("""
      select new com.robofleet.fleetstateservice.robot.application.dto.RobotSummaryResponse(
        r.robotId,
        r.displayName,
        r.lifecycleStatus
      )
      from Robot r
      """)
  Page<RobotSummaryResponse> findAllRobotSummaries(Pageable pageable);

  /**
   * Returns projected robot summary for one robot id.
   */
  @Query("""
      select new com.robofleet.fleetstateservice.robot.application.dto.RobotSummaryResponse(
        r.robotId,
        r.displayName,
        r.lifecycleStatus
      )
      from Robot r
      where r.robotId = :robotId
      """)
  Optional<RobotSummaryResponse> findRobotSummaryByRobotId(@Param("robotId") String robotId);
}
