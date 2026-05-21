package com.robofleet.robotsimulator.robot.infrastructure.messaging.event;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;

class RobotStateChangedEventTest {

  private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

  @Test
  void shouldReturnSchemaVersionV1() {
    RobotStateChangedEvent event = RobotStateChangedEvent.builder()
        .eventName("robot-state-changed")
        .correlationId("corr-1")
        .robotId("robot-1")
        .positionX(10.0)
        .positionY(20.0)
        .battery(80.0)
        .status("MOVING")
        .timestamp(Instant.parse("2026-05-19T16:40:03Z"))
        .build();

    assertEquals(SchemaVersion.V1, event.getSchemaVersion());
  }

  @Test
  void shouldSerializeSchemaVersionAsReadOnlyV1() throws Exception {
    RobotStateChangedEvent event = RobotStateChangedEvent.builder()
        .eventName("robot-state-changed")
        .correlationId("corr-1")
        .robotId("robot-1")
        .positionX(10.0)
        .positionY(20.0)
        .battery(80.0)
        .status("MOVING")
        .timestamp(Instant.parse("2026-05-19T16:40:03Z"))
        .build();

    String json = objectMapper.writeValueAsString(event);

    JSONAssert.assertEquals("{\"schemaVersion\":\"v1\"}", json, false);
  }
}
