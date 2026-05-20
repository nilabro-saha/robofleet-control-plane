package com.robofleet.fleetstateservice.robot.infrastructure.messaging;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Duration;
import java.time.Instant;

import static org.awaitility.Awaitility.await;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("itest")
@EmbeddedKafka(partitions = 1, topics = {"robot.telemetry"})
@AutoConfigureMockMvc
@DirtiesContext
@Sql(scripts = "/sql/itest-reset.sql")
class RobotTelemetryKafkaDbITest {

  @Autowired
  private KafkaTemplate<String, RobotStateChangedEvent> kafkaTemplate;

  @Autowired
  private MockMvc mockMvc;

  @Test
  void shouldInsertAndThenUpdateRobotStateFromKafkaTelemetry() {
    String robotId = "robot-kafka-1";

    RobotStateChangedEvent first = RobotStateChangedEvent.builder()
        .robotId(robotId)
        .displayName("Gamma")
        .positionX(10.0)
        .positionY(20.0)
        .battery(90.0)
        .status("IDLE")
        .timestamp(Instant.parse("2026-05-20T00:00:00Z"))
        .build();

    kafkaTemplate.send("robot.telemetry", robotId, first);
    kafkaTemplate.flush();

    await()
        .atMost(Duration.ofSeconds(10))
        .untilAsserted(() -> {
          mockMvc.perform(get("/api/robots/{id}", robotId))
              .andExpect(status().isOk())
              .andExpect(jsonPath("$.x").value(10.0))
              .andExpect(jsonPath("$.y").value(20.0))
              .andExpect(jsonPath("$.displayName").value("Gamma"))
              .andExpect(jsonPath("$.battery").value(90.0))
              .andExpect(jsonPath("$.status").value("IDLE"));
        });

    RobotStateChangedEvent second = RobotStateChangedEvent.builder()
        .robotId(robotId)
        .displayName(null)
        .positionX(30.0)
        .positionY(40.0)
        .battery(55.5)
        .status("MOVING")
        .timestamp(Instant.parse("2026-05-20T00:00:05Z"))
        .build();

    kafkaTemplate.send("robot.telemetry", robotId, second);
    kafkaTemplate.flush();

    await()
        .atMost(Duration.ofSeconds(10))
        .untilAsserted(() -> {
          mockMvc.perform(get("/api/robots/{id}", robotId))
              .andExpect(status().isOk())
              .andExpect(jsonPath("$.x").value(30.0))
              .andExpect(jsonPath("$.y").value(40.0))
              .andExpect(jsonPath("$.displayName").value("Gamma"))
              .andExpect(jsonPath("$.battery").value(55.5))
              .andExpect(jsonPath("$.status").value("MOVING"));
        });
  }
}
