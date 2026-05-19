package com.robofleet.fleetstateservice.robot.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Persistence model of "latest known state" per robot.
 *
 * <p>Intent: represent the control-plane snapshot table, not an event history table.
 * Each row is overwritten as newer telemetry arrives for the same robot.</p>
 */
@Entity
@Table(name = "robot_state")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class RobotState {

  @Id
  @Column(name = "robot_id", nullable = false, length = 100)
  private String robotId;

  @Column(name = "x", nullable = false)
  private double positionX;

  @Column(name = "y", nullable = false)
  private double positionY;

  @Column(name = "battery", nullable = false)
  private double battery;

  @Column(name = "status", nullable = false, length = 30)
  private String status;

  @Column(name = "timestamp", nullable = false)
  private Instant timestamp;
}
