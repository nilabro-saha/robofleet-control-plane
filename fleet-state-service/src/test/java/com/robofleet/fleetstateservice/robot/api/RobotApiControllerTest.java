package com.robofleet.fleetstateservice.robot.api;

import com.robofleet.fleetstateservice.robot.application.RobotStateService;
import com.robofleet.fleetstateservice.robot.application.dto.RobotStateResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RobotApiControllerTest {

  @Mock
  private RobotStateService robotStateService;

  @InjectMocks
  private RobotApiController robotApiController;

  @Test
  void shouldReturnAllRobots() {
    Pageable pageable = PageRequest.of(0, 100, Sort.by(Sort.Direction.ASC, "robotId"));
    when(robotStateService.getAllRobots(pageable)).thenReturn(List.of(sampleResponse()));

    List<RobotStateResponse> response = robotApiController.getAllRobots(pageable);

    assertEquals(1, response.size());
    assertEquals("robot-1", response.get(0).getRobotId());
    verify(robotStateService).getAllRobots(pageable);
  }

  @Test
  void shouldReturnRobotByIdWhenPresent() {
    when(robotStateService.getRobotById("robot-1")).thenReturn(Optional.of(sampleResponse()));

    ResponseEntity<RobotStateResponse> response = robotApiController.getRobotById("robot-1");

    assertEquals(HttpStatusCode.valueOf(200), response.getStatusCode());
    assertEquals("robot-1", response.getBody().getRobotId());
  }

  @Test
  void shouldReturn404WhenRobotNotFound() {
    when(robotStateService.getRobotById("missing")).thenReturn(Optional.empty());

    ResponseEntity<RobotStateResponse> response = robotApiController.getRobotById("missing");

    assertEquals(HttpStatusCode.valueOf(404), response.getStatusCode());
  }

  private RobotStateResponse sampleResponse() {
    return RobotStateResponse.builder()
        .robotId("robot-1")
        .positionX(12.34)
        .positionY(56.78)
        .battery(87.1)
        .status("MOVING")
        .timestamp(Instant.parse("2026-05-19T16:40:03Z"))
        .build();
  }
}
