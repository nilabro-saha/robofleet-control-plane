package com.robofleet.fleetstateservice.robot.api;

import com.robofleet.fleetstateservice.robot.application.RobotStateService;
import com.robofleet.fleetstateservice.robot.application.dto.CreateRobotRequest;
import com.robofleet.fleetstateservice.robot.application.dto.RobotCreationResponse;
import com.robofleet.fleetstateservice.robot.application.dto.RobotSummaryResponse;
import com.robofleet.fleetstateservice.robot.application.dto.RobotStateResponse;
import com.robofleet.fleetstateservice.robot.domain.RobotLifecycleStatus;
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
  void shouldReturnAllRobotStatuses() {
    Pageable pageable = PageRequest.of(0, 100, Sort.by(Sort.Direction.ASC, "robotId"));
    when(robotStateService.getAllRobotStatuses(pageable)).thenReturn(List.of(sampleStatusResponse()));

    List<RobotStateResponse> response = robotApiController.getAllRobotStatuses(pageable);

    assertEquals(1, response.size());
    assertEquals("robot-1", response.get(0).getRobotId());
    assertEquals("Alpha", response.get(0).getDisplayName());
    verify(robotStateService).getAllRobotStatuses(pageable);
  }

  @Test
  void shouldReturnRobotStatusByIdWhenPresent() {
    when(robotStateService.getRobotStatusById("robot-1")).thenReturn(Optional.of(sampleStatusResponse()));

    ResponseEntity<RobotStateResponse> response = robotApiController.getRobotStatusById("robot-1");

    assertEquals(HttpStatusCode.valueOf(200), response.getStatusCode());
    assertEquals("robot-1", response.getBody().getRobotId());
    assertEquals("Alpha", response.getBody().getDisplayName());
  }

  @Test
  void shouldReturn404WhenRobotStatusNotFound() {
    when(robotStateService.getRobotStatusById("missing")).thenReturn(Optional.empty());

    ResponseEntity<RobotStateResponse> response = robotApiController.getRobotStatusById("missing");

    assertEquals(HttpStatusCode.valueOf(404), response.getStatusCode());
  }

  @Test
  void shouldReturnAllRobots() {
    Pageable pageable = PageRequest.of(0, 100, Sort.by(Sort.Direction.ASC, "robotId"));
    when(robotStateService.getAllRobots(pageable)).thenReturn(List.of(sampleRobotSummary()));

    List<RobotSummaryResponse> response = robotApiController.getAllRobots(pageable);

    assertEquals(1, response.size());
    assertEquals("robot-1", response.get(0).getRobotId());
    assertEquals(RobotLifecycleStatus.ACTIVE, response.get(0).getLifecycleStatus());
  }

  @Test
  void shouldReturnRobotByIdWhenPresent() {
    when(robotStateService.getRobotById("robot-1")).thenReturn(Optional.of(sampleRobotSummary()));

    ResponseEntity<RobotSummaryResponse> response = robotApiController.getRobotById("robot-1");

    assertEquals(HttpStatusCode.valueOf(200), response.getStatusCode());
    assertEquals("robot-1", response.getBody().getRobotId());
  }

  @Test
  void shouldReturn404WhenRobotSummaryNotFound() {
    when(robotStateService.getRobotById("missing")).thenReturn(Optional.empty());

    ResponseEntity<RobotSummaryResponse> response = robotApiController.getRobotById("missing");

    assertEquals(HttpStatusCode.valueOf(404), response.getStatusCode());
  }

  @Test
  void shouldAcceptCreateRobotRequest() {
    CreateRobotRequest request = CreateRobotRequest.builder()
        .displayName("NinetyNine")
        .build();
    when(robotStateService.createRobot(request))
        .thenReturn(new RobotCreationResponse("generated-id", "NinetyNine", "CREATE_PENDING"));

    ResponseEntity<RobotCreationResponse> response = robotApiController.createRobot(request);

    assertEquals(HttpStatusCode.valueOf(202), response.getStatusCode());
    assertEquals("generated-id", response.getBody().robotId());
  }

  private RobotStateResponse sampleStatusResponse() {
    return RobotStateResponse.builder()
        .robotId("robot-1")
        .displayName("Alpha")
        .positionX(12.34)
        .positionY(56.78)
        .battery(87.1)
        .lifecycleStatus(RobotLifecycleStatus.ACTIVE)
        .status("MOVING")
        .timestamp(Instant.parse("2026-05-19T16:40:03Z"))
        .build();
  }

  private RobotSummaryResponse sampleRobotSummary() {
    return RobotSummaryResponse.builder()
        .robotId("robot-1")
        .displayName("Alpha")
        .lifecycleStatus(RobotLifecycleStatus.ACTIVE)
        .build();
  }
}
