package com.robofleet.fleetstateservice.robot.infrastructure.persistence;

import com.robofleet.fleetstateservice.robot.domain.RobotState;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RobotStateRepository extends JpaRepository<RobotState, String> {
}
