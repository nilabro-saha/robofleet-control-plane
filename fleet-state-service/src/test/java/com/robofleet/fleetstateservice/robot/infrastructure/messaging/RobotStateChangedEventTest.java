package com.robofleet.fleetstateservice.robot.infrastructure.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RobotStateChangedEventTest {

  private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

  @Test
  void shouldDeserializeFromJsonPayload() throws Exception {
    String json = """
        {
          "robotId": "robot-1",
          "x": 12.34,
          "y": 56.78,
          "battery": 87.1,
          "status": "MOVING",
          "timestamp": "2026-05-19T16:40:03Z"
        }
        """;

    RobotStateChangedEvent event = objectMapper.readValue(json, RobotStateChangedEvent.class);

    assertEquals("robot-1", event.getRobotId());
    assertEquals(12.34, event.getPositionX());
    assertEquals(56.78, event.getPositionY());
    assertEquals(87.1, event.getBattery());
    assertEquals("MOVING", event.getStatus());
    assertEquals("2026-05-19T16:40:03Z", event.getTimestamp().toString());
  }
}
