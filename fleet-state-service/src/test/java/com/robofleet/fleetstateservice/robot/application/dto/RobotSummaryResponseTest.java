package com.robofleet.fleetstateservice.robot.application.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.robofleet.fleetstateservice.robot.domain.RobotLifecycleStatus;
import org.junit.jupiter.api.Test;

class RobotSummaryResponseTest {

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Test
  void shouldSerializeToExpectedJsonPayload() throws Exception {
    RobotSummaryResponse response = RobotSummaryResponse.builder()
        .robotId("robot-1")
        .displayName("Alpha")
        .lifecycleStatus(RobotLifecycleStatus.ACTIVE)
        .build();

    JsonNode node = objectMapper.readTree(objectMapper.writeValueAsString(response));

    assertEquals("robot-1", node.get("robotId").asText());
    assertEquals("Alpha", node.get("displayName").asText());
    assertEquals("ACTIVE", node.get("lifecycleStatus").asText());
  }
}
