package com.robofleet.fleetstateservice.robot.infrastructure.persistence;

import com.robofleet.fleetstateservice.robot.domain.RobotState;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Persistence access point for current fleet snapshot data.
 *
 * <p>Intent: rely on Spring Data defaults for MVP simplicity; custom query
 * complexity is intentionally deferred until needed.</p>
 */
public interface RobotStateRepository extends JpaRepository<RobotState, String> {
}
