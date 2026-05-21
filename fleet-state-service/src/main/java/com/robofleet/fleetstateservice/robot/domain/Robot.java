package com.robofleet.fleetstateservice.robot.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Master robot table containing robot identity attributes.
 */
@Entity
@Table(name = "robot")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Robot {

  @Id
  @Column(name = "robot_id", nullable = false, length = 100)
  private String robotId;

  @Column(name = "display_name", nullable = true, length = 120)
  private String displayName;

  @Enumerated(EnumType.STRING)
  @Column(name = "lifecycle_status", nullable = false, length = 30)
  private RobotLifecycleStatus lifecycleStatus;

  /**
   * Marks this robot as active.
   */
  public void markAsActive() {
    this.lifecycleStatus = RobotLifecycleStatus.ACTIVE;
  }

  /**
   * Marks this robot as pending deletion.
   */
  public void markAsDeletePending() {
    this.lifecycleStatus = RobotLifecycleStatus.DELETE_PENDING;
  }
}
