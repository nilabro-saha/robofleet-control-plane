package com.robofleet.fleetstateservice.robot.infrastructure.persistence;

import com.robofleet.fleetstateservice.robot.domain.Robot;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Persistence access point for master robot identity rows.
 */
public interface RobotRepository extends JpaRepository<Robot, String> {
}
