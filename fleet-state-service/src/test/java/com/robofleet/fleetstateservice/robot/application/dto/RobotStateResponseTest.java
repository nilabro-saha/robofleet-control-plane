package com.robofleet.fleetstateservice.robot.application.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.robofleet.fleetstateservice.robot.domain.RobotLifecycleStatus;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RobotStateResponseTest {

  private final ObjectMapper objectMapper = new ObjectMapper()
      .findAndRegisterModules()
      .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

  @Test
  void shouldSerializeToExpectedJsonPayload() throws Exception {
    RobotStateResponse response = RobotStateResponse.builder()
        .robotId("robot-1")
        .displayName("Alpha")
        .positionX(12.34)
        .positionY(56.78)
        .battery(87.1)
        .lifecycleStatus(RobotLifecycleStatus.ACTIVE)
        .status("MOVING")
        .timestamp(Instant.parse("2026-05-19T16:40:03Z"))
        .build();

    JsonNode node = objectMapper.readTree(objectMapper.writeValueAsString(response));

    assertEquals("robot-1", node.get("robotId").asText());
    assertEquals("Alpha", node.get("displayName").asText());
    assertEquals(12.34, node.get("x").asDouble());
    assertEquals(56.78, node.get("y").asDouble());
    assertEquals(87.1, node.get("battery").asDouble());
    assertEquals("ACTIVE", node.get("lifecycleStatus").asText());
    assertEquals("MOVING", node.get("status").asText());
    assertEquals("2026-05-19T16:40:03Z", node.get("timestamp").asText());
  }
}
