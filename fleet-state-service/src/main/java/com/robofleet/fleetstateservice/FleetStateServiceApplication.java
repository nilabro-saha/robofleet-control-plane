package com.robofleet.fleetstateservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the fleet state materialization service.
 *
 * <p>Intent: keep this service narrowly focused on one control-plane concern —
 * continuously converting telemetry events into "latest known robot state"
 * that can be queried quickly by dashboards or operator tools.</p>
 */
@SpringBootApplication
public class FleetStateServiceApplication {

  public static void main(String[] args) {
    SpringApplication.run(FleetStateServiceApplication.class, args);
  }
}
