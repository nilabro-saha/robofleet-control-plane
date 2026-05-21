package com.robofleet.fleetstateservice.robot.infrastructure.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RobotStateChangedEventTest {

  private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

  @Test
  void shouldDeserializeFromJsonPayload() throws Exception {
    String json = """
        {
          "eventName": "robot-state-changed",
          "correlationId": "corr-1",
          "robotId": "robot-1",
          "x": 12.34,
          "y": 56.78,
          "battery": 87.1,
          "status": "MOVING",
          "timestamp": "2026-05-19T16:40:03Z"
        }
        """;

    RobotStateChangedEvent event = objectMapper.readValue(json, RobotStateChangedEvent.class);

    assertEquals("robot-state-changed", event.getEventName());
    assertEquals("corr-1", event.getCorrelationId());
    assertEquals("robot-1", event.getRobotId());
    assertEquals(12.34, event.getPositionX());
    assertEquals(56.78, event.getPositionY());
    assertEquals(87.1, event.getBattery());
    assertEquals("MOVING", event.getStatus());
    assertEquals("2026-05-19T16:40:03Z", event.getTimestamp().toString());
    assertEquals(SchemaVersion.V1, event.getSchemaVersion());
  }

  @Test
  void shouldSerializeSchemaVersionAsReadOnlyV1() throws Exception {
    RobotStateChangedEvent event = RobotStateChangedEvent.builder()
        .eventName("robot-state-changed")
        .correlationId("corr-1")
        .robotId("robot-1")
        .positionX(12.34)
        .positionY(56.78)
        .battery(87.1)
        .status("MOVING")
        .timestamp(java.time.Instant.parse("2026-05-19T16:40:03Z"))
        .build();

    String json = objectMapper.writeValueAsString(event);

    JSONAssert.assertEquals("{\"schemaVersion\":\"v1\"}", json, false);
  }
}
