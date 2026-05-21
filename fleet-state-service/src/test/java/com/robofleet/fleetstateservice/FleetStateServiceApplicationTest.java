package com.robofleet.fleetstateservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "fleet.state.rehydration.publish-on-startup-enabled=false")
class FleetStateServiceApplicationTest {

  @Test
  void contextLoads() {
    // context load smoke test
  }
}
