package com.robofleet.fleetstateservice.robot.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("itest")
@Sql(scripts = "/sql/itest-seed.sql")
@AutoConfigureMockMvc
class RobotApiControllerITest {

  @Autowired
  private MockMvc mockMvc;

  @Test
  void shouldReturnRobotStatusesCollection() throws Exception {
    mockMvc.perform(get("/api/robot-statuses").accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].robotId").value("robot-1"))
        .andExpect(jsonPath("$[0].displayName").value("Alpha"))
        .andExpect(jsonPath("$[0].lifecycleStatus").value("ACTIVE"))
        .andExpect(jsonPath("$[0].x").value(12.34))
        .andExpect(jsonPath("$[0].y").value(56.78));
  }

  @Test
  void shouldReturnRobotStatusesSortedByBatteryDescending() throws Exception {
    mockMvc.perform(get("/api/robot-statuses")
            .param("sort", "battery,desc")
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].robotId").value("robot-1"))
        .andExpect(jsonPath("$[1].robotId").value("robot-2"));
  }

  @Test
  void shouldReturnSingleRobotStatusWhenPresent() throws Exception {
    mockMvc.perform(get("/api/robot-statuses/robot-1").accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.robotId").value("robot-1"))
        .andExpect(jsonPath("$.displayName").value("Alpha"))
        .andExpect(jsonPath("$.lifecycleStatus").value("ACTIVE"));
  }

  @Test
  void shouldReturnNotFoundWhenRobotStatusIsMissing() throws Exception {
    mockMvc.perform(get("/api/robot-statuses/missing").accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound());
  }

  @Test
  void shouldReturnRobotsCollection() throws Exception {
    mockMvc.perform(get("/api/robots").accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].robotId").value("robot-1"))
        .andExpect(jsonPath("$[0].displayName").value("Alpha"))
        .andExpect(jsonPath("$[0].lifecycleStatus").value("ACTIVE"));
  }

  @Test
  void shouldReturnNotFoundWhenRobotIsMissing() throws Exception {
    mockMvc.perform(get("/api/robots/missing").accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound());
  }
}
