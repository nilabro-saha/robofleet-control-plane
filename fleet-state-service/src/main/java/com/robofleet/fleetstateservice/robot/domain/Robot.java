package com.robofleet.fleetstateservice.robot.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
}
