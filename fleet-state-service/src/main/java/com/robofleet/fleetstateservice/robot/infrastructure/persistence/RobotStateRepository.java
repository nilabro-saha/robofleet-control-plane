package com.robofleet.fleetstateservice.robot.infrastructure.persistence;

import com.robofleet.fleetstateservice.robot.domain.RobotState;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Persistence access point for current fleet snapshot data.
 *
 * <p>Intent: rely on Spring Data defaults for MVP simplicity; custom query
 * complexity is intentionally deferred until needed.</p>
 */
public interface RobotStateRepository extends JpaRepository<RobotState, String> {

  /**
   * Returns pageable latest-state rows for fleet snapshot queries.
   *
   * @param pageable pagination and sorting request
   * @return page of robot latest-state entities
   */
  Page<RobotState> findAll(Pageable pageable);
}
