package com.robofleet.fleetstateservice.robot.application;

import com.robofleet.fleetstateservice.robot.application.dto.RobotStateResponse;
import com.robofleet.fleetstateservice.robot.domain.Robot;
import com.robofleet.fleetstateservice.robot.domain.RobotState;
import com.robofleet.fleetstateservice.robot.infrastructure.messaging.RobotStateChangedEvent;
import com.robofleet.fleetstateservice.robot.infrastructure.persistence.RobotRepository;
import com.robofleet.fleetstateservice.robot.infrastructure.persistence.RobotStateRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class RobotStateServiceImplTest {

  @Mock
  private RobotRepository robotRepository;

  @Mock
  private RobotStateRepository robotStateRepository;

  @InjectMocks
  private RobotStateServiceImpl robotStateService;

  @Test
  void shouldUpsertFromTelemetry() {
    Instant timestamp = Instant.parse("2026-05-19T16:40:03Z");
    RobotStateChangedEvent event = RobotStateChangedEvent.builder()
        .robotId("robot-1")
        .displayName("Alpha")
        .positionX(12.34)
        .positionY(56.78)
        .battery(87.1)
        .status("MOVING")
        .timestamp(timestamp)
        .build();

    when(robotRepository.findById("robot-1")).thenReturn(Optional.empty());

    robotStateService.upsertFromTelemetry(event);

    ArgumentCaptor<RobotState> captor = ArgumentCaptor.forClass(RobotState.class);
    verify(robotRepository).save(org.mockito.ArgumentMatchers.any(Robot.class));
    verify(robotStateRepository).save(captor.capture());
    RobotState saved = captor.getValue();
    assertEquals("robot-1", saved.getRobotId());
    assertEquals(12.34, saved.getPositionX());
    assertEquals(56.78, saved.getPositionY());
    assertEquals(87.1, saved.getBattery());
    assertEquals("MOVING", saved.getStatus());
    assertEquals(timestamp, saved.getTimestamp());
  }

  @Test
  void shouldReturnAllRobots() {
    Instant timestamp = Instant.parse("2026-05-19T16:40:03Z");
    Pageable pageable = PageRequest.of(0, 100, Sort.by(Sort.Direction.ASC, "robotId"));
    when(robotStateRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(
        RobotState.builder()
            .robotId("robot-1")
            .positionX(12.34)
            .positionY(56.78)
            .battery(87.1)
            .status("MOVING")
            .timestamp(timestamp)
            .build()
    )));
    when(robotRepository.findById("robot-1"))
        .thenReturn(Optional.of(Robot.builder().robotId("robot-1").displayName("Alpha").build()));

    List<RobotStateResponse> response = robotStateService.getAllRobots(pageable);

    assertEquals(1, response.size());
    assertEquals("robot-1", response.get(0).getRobotId());
    assertEquals("Alpha", response.get(0).getDisplayName());
    assertEquals(12.34, response.get(0).getPositionX());
    assertEquals(56.78, response.get(0).getPositionY());

    verify(robotStateRepository).findAll(pageable);
  }

  @Test
  void shouldUsePageableSortingFromCaller() {
    Pageable pageable = PageRequest.of(0, 100, Sort.by(Sort.Direction.DESC, "battery"));
    when(robotStateRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of()));

    robotStateService.getAllRobots(pageable);

    verify(robotStateRepository).findAll(pageable);
  }

  @Test
  void shouldReturnRobotByIdWhenFound() {
    Instant timestamp = Instant.parse("2026-05-19T16:40:03Z");
    when(robotStateRepository.findById("robot-1")).thenReturn(Optional.of(
        RobotState.builder()
            .robotId("robot-1")
            .positionX(12.34)
            .positionY(56.78)
            .battery(87.1)
            .status("MOVING")
            .timestamp(timestamp)
            .build()
    ));
    when(robotRepository.findById("robot-1"))
        .thenReturn(Optional.of(Robot.builder().robotId("robot-1").displayName(null).build()));

    Optional<RobotStateResponse> response = robotStateService.getRobotById("robot-1");

    assertTrue(response.isPresent());
    assertEquals("robot-1", response.get().getRobotId());
  }

  @Test
  void shouldRemoveRobotById() {
    robotStateService.removeRobotById("robot-1");

    verify(robotStateRepository, times(1)).deleteById("robot-1");
    verify(robotRepository, times(1)).deleteById("robot-1");
  }

  @Test
  void shouldPreserveExistingDisplayNameWhenTelemetryDisplayNameIsNull() {
    Instant timestamp = Instant.parse("2026-05-19T16:40:03Z");
    RobotStateChangedEvent event = RobotStateChangedEvent.builder()
        .robotId("robot-1")
        .displayName(null)
        .positionX(12.34)
        .positionY(56.78)
        .battery(87.1)
        .status("MOVING")
        .timestamp(timestamp)
        .build();

    when(robotRepository.findById("robot-1"))
        .thenReturn(Optional.of(Robot.builder().robotId("robot-1").displayName("Persisted").build()));

    robotStateService.upsertFromTelemetry(event);

    ArgumentCaptor<Robot> robotCaptor = ArgumentCaptor.forClass(Robot.class);
    verify(robotRepository).save(robotCaptor.capture());
    assertEquals("Persisted", robotCaptor.getValue().getDisplayName());
    verify(robotStateRepository).save(any(RobotState.class));
  }
}
