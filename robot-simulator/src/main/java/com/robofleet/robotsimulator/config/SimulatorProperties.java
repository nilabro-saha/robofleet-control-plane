package com.robofleet.robotsimulator.config;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Configuration properties for robot simulator runtime behavior.
 */
@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "robot.simulator")
public class SimulatorProperties {

  /**
   * Number of robots to spawn during startup.
   */
  @Min(0)
  private int robotCount = 0;

  /**
   * Telemetry publish interval in milliseconds.
   */
  @Min(100)
  private long telemetryIntervalMs = 1000;

  /**
   * Robot state advancement interval in milliseconds.
   */
  @Min(100)
  private long stateAdvanceIntervalMs = 1000;

  /**
   * Rectangular map boundary minimum X coordinate.
   */
  @DecimalMin("0.0")
  private double mapMinX = 0.0;

  /**
   * Rectangular map boundary maximum X coordinate.
   */
  private double mapMaxX = 100.0;

  /**
   * Rectangular map boundary minimum Y coordinate.
   */
  @DecimalMin("0.0")
  private double mapMinY = 0.0;

  /**
   * Rectangular map boundary maximum Y coordinate.
   */
  private double mapMaxY = 100.0;

}
