package com.robofleet.fleetstateservice.robot.application;

import com.robofleet.fleetstateservice.robot.application.dto.RobotStateResponse;
import com.robofleet.fleetstateservice.robot.application.dto.CreateRobotRequest;
import com.robofleet.fleetstateservice.robot.application.dto.RobotCreationResponse;
import com.robofleet.fleetstateservice.robot.application.dto.RobotSummaryResponse;
import com.robofleet.fleetstateservice.robot.domain.Robot;
import com.robofleet.fleetstateservice.robot.domain.RobotLifecycleStatus;
import com.robofleet.fleetstateservice.robot.domain.RobotState;
import com.robofleet.fleetstateservice.robot.infrastructure.messaging.RobotLifecycleEvent;
import com.robofleet.fleetstateservice.robot.infrastructure.messaging.LifecycleEventType;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class RobotStateServiceImplTest {

  @Mock
  private RobotRepository robotRepository;

  @Mock
  private RobotStateRepository robotStateRepository;

  @Mock
  private RobotCreationCommandGateway robotCreationCommandGateway;

  @InjectMocks
  private RobotStateServiceImpl robotStateService;

  @Test
  void shouldUpsertFromTelemetry() {
    Instant timestamp = Instant.parse("2026-05-19T16:40:03Z");
    RobotStateChangedEvent event = RobotStateChangedEvent.builder()
        .robotId("robot-1")
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
    when(robotRepository.findAllRobotSummaries(pageable)).thenReturn(new PageImpl<>(List.of(
        RobotSummaryResponse.builder()
            .robotId("robot-1")
            .displayName("Alpha")
            .lifecycleStatus(RobotLifecycleStatus.ACTIVE)
            .build()
    )));

    List<RobotSummaryResponse> response = robotStateService.getAllRobots(pageable);

    assertEquals(1, response.size());
    assertEquals("robot-1", response.get(0).getRobotId());
    assertEquals("Alpha", response.get(0).getDisplayName());
    assertEquals(RobotLifecycleStatus.ACTIVE, response.get(0).getLifecycleStatus());

    verify(robotRepository).findAllRobotSummaries(pageable);
  }

  @Test
  void shouldUsePageableSortingFromCaller() {
    Pageable pageable = PageRequest.of(0, 100, Sort.by(Sort.Direction.DESC, "battery"));
    when(robotRepository.findAllRobotSummaries(pageable)).thenReturn(new PageImpl<>(List.of()));

    robotStateService.getAllRobots(pageable);

    verify(robotRepository).findAllRobotSummaries(pageable);
  }

  @Test
  void shouldReturnRobotByIdWhenFound() {
    when(robotRepository.findRobotSummaryByRobotId("robot-1"))
        .thenReturn(Optional.of(RobotSummaryResponse.builder()
            .robotId("robot-1")
            .displayName(null)
            .lifecycleStatus(RobotLifecycleStatus.ACTIVE)
            .build()
        ));

    Optional<RobotSummaryResponse> response = robotStateService.getRobotById("robot-1");

    assertTrue(response.isPresent());
    assertEquals("robot-1", response.get().getRobotId());
  }

  @Test
  void shouldReturnEmptyWhenRobotByIdNotFound() {
    when(robotRepository.findRobotSummaryByRobotId("missing")).thenReturn(Optional.empty());

    Optional<RobotSummaryResponse> response = robotStateService.getRobotById("missing");

    assertTrue(response.isEmpty());
  }

  @Test
  void shouldReturnAllRobotStatuses() {
    Instant timestamp = Instant.parse("2026-05-19T16:40:03Z");
    Pageable pageable = PageRequest.of(0, 100, Sort.by(Sort.Direction.ASC, "robotId"));
    when(robotStateRepository.findAllRobotStatuses(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(
        RobotStateResponse.builder()
            .robotId("robot-1")
            .displayName("Alpha")
            .positionX(12.34)
            .positionY(56.78)
            .battery(87.1)
            .lifecycleStatus(RobotLifecycleStatus.ACTIVE)
            .status("MOVING")
            .timestamp(timestamp)
            .build()
    )));

    List<RobotStateResponse> response = robotStateService.getAllRobotStatuses(pageable);

    assertEquals(1, response.size());
    assertEquals("robot-1", response.get(0).getRobotId());
    assertEquals("Alpha", response.get(0).getDisplayName());

    ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
    verify(robotStateRepository).findAllRobotStatuses(pageableCaptor.capture());
    Sort.Order sortOrder = pageableCaptor.getValue().getSort().getOrderFor("robot.robotId");
    assertEquals(Sort.Direction.ASC, sortOrder.getDirection());
  }

  @Test
  void shouldMapRobotStatusSortFieldsAcrossJoinedEntities() {
    Pageable pageable = PageRequest.of(
        0,
        100,
        Sort.by(
            Sort.Order.desc("x"),
            Sort.Order.asc("displayName")
        )
    );
    when(robotStateRepository.findAllRobotStatuses(any(Pageable.class)))
        .thenReturn(new PageImpl<>(List.of()));

    robotStateService.getAllRobotStatuses(pageable);

    ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
    verify(robotStateRepository).findAllRobotStatuses(pageableCaptor.capture());
    Sort mappedSort = pageableCaptor.getValue().getSort();
    assertEquals(Sort.Direction.DESC, mappedSort.getOrderFor("state.positionX").getDirection());
    assertEquals(Sort.Direction.ASC, mappedSort.getOrderFor("robot.displayName").getDirection());
  }

  @Test
  void shouldReturnRobotStatusByIdWhenFound() {
    Instant timestamp = Instant.parse("2026-05-19T16:40:03Z");
    when(robotStateRepository.findRobotStatusByRobotId("robot-1"))
        .thenReturn(Optional.of(RobotStateResponse.builder()
            .robotId("robot-1")
            .displayName("Alpha")
            .positionX(12.34)
            .positionY(56.78)
            .battery(87.1)
            .lifecycleStatus(RobotLifecycleStatus.ACTIVE)
            .status("MOVING")
            .timestamp(timestamp)
            .build()));

    Optional<RobotStateResponse> response = robotStateService.getRobotStatusById("robot-1");

    assertTrue(response.isPresent());
    assertEquals("robot-1", response.get().getRobotId());
    verify(robotStateRepository).findRobotStatusByRobotId("robot-1");
  }

  @Test
  void shouldReturnEmptyWhenRobotStatusByIdNotFound() {
    when(robotStateRepository.findRobotStatusByRobotId("missing")).thenReturn(Optional.empty());

    Optional<RobotStateResponse> response = robotStateService.getRobotStatusById("missing");

    assertTrue(response.isEmpty());
  }

  @Test
  void shouldRemoveRobotById() {
    robotStateService.removeRobotById("robot-1");

    verify(robotStateRepository, times(1)).deleteById("robot-1");
    verify(robotRepository, times(1)).deleteById("robot-1");
  }

  @Test
  void shouldApplyRemovedLifecycleEventByRemovingRobot() {
    RobotLifecycleEvent event = RobotLifecycleEvent.builder()
        .robotId("robot-1")
        .eventType(LifecycleEventType.REMOVED)
        .build();

    robotStateService.applyRemovedLifecycleEvent(event);

    verify(robotStateRepository).deleteById("robot-1");
    verify(robotRepository).deleteById("robot-1");
  }

  @Test
  void shouldPreserveExistingDisplayNameWhenTelemetryIsConsumed() {
    Instant timestamp = Instant.parse("2026-05-19T16:40:03Z");
    RobotStateChangedEvent event = RobotStateChangedEvent.builder()
        .robotId("robot-1")
        .positionX(12.34)
        .positionY(56.78)
        .battery(87.1)
        .status("MOVING")
        .timestamp(timestamp)
        .build();

    when(robotRepository.findById("robot-1"))
        .thenReturn(Optional.of(Robot.builder()
            .robotId("robot-1")
            .displayName("Persisted")
            .lifecycleStatus(RobotLifecycleStatus.ACTIVE)
            .build()));

    robotStateService.upsertFromTelemetry(event);

    ArgumentCaptor<Robot> robotCaptor = ArgumentCaptor.forClass(Robot.class);
    verify(robotRepository).save(robotCaptor.capture());
    assertEquals("Persisted", robotCaptor.getValue().getDisplayName());
    verify(robotStateRepository).save(any(RobotState.class));
  }

  @Test
  void shouldCreateRobotAsPendingAndPublishCommand() {
    CreateRobotRequest request = CreateRobotRequest.builder()
        .displayName("NinetyNine")
        .build();

    RobotCreationResponse response = robotStateService.createRobot(request);

    assertTrue(response.robotId() != null && !response.robotId().isBlank());
    assertEquals("NinetyNine", response.displayName());
    assertEquals("CREATE_PENDING", response.lifecycleStatus());
    verify(robotRepository).save(any(Robot.class));
    verify(robotCreationCommandGateway).publishLifecycleEvent(any());
  }

  @Test
  void shouldUpsertWithActiveLifecycleWhenNoExistingRobot() {
    Instant timestamp = Instant.parse("2026-05-19T16:40:03Z");
    RobotStateChangedEvent event = RobotStateChangedEvent.builder()
        .robotId("robot-new")
        .positionX(10.0)
        .positionY(20.0)
        .battery(30.0)
        .status("IDLE")
        .timestamp(timestamp)
        .build();

    when(robotRepository.findById("robot-new")).thenReturn(Optional.empty());

    robotStateService.upsertFromTelemetry(event);

    ArgumentCaptor<Robot> robotCaptor = ArgumentCaptor.forClass(Robot.class);
    verify(robotRepository).save(robotCaptor.capture());
    assertEquals(RobotLifecycleStatus.ACTIVE, robotCaptor.getValue().getLifecycleStatus());
  }

  @Test
  void shouldDoNothingWhenCreatedLifecycleEventRobotIsUnknown() {
    Instant now = Instant.parse("2026-05-19T16:40:03Z");
    RobotLifecycleEvent event = RobotLifecycleEvent.builder()
        .robotId("robot-1")
        .positionX(1.2)
        .positionY(3.4)
        .battery(88.8)
        .status("IDLE")
        .eventType(LifecycleEventType.CREATED)
        .timestamp(now)
        .build();
    when(robotRepository.findById("robot-1")).thenReturn(Optional.empty());

    robotStateService.applyCreatedLifecycleEvent(event);

    verify(robotRepository, never()).save(any(Robot.class));
    verify(robotStateRepository, never()).save(any(RobotState.class));
  }

  @Test
  void shouldMarkRobotActiveWhenCreatedLifecycleEventRobotExists() {
    Instant now = Instant.parse("2026-05-19T16:40:03Z");
    RobotLifecycleEvent event = RobotLifecycleEvent.builder()
        .robotId("robot-1")
        .positionX(11.1)
        .positionY(22.2)
        .battery(77.7)
        .status("IDLE")
        .eventType(LifecycleEventType.CREATED)
        .timestamp(now)
        .build();
    Robot existingRobot = Robot.builder()
        .robotId("robot-1")
        .displayName("Alpha")
        .lifecycleStatus(RobotLifecycleStatus.CREATE_PENDING)
        .build();
    RobotState existingState = RobotState.builder()
        .robotId("robot-1")
        .positionX(1.0)
        .positionY(2.0)
        .battery(3.0)
        .status("MOVING")
        .timestamp(Instant.parse("2026-05-19T16:00:00Z"))
        .build();
    when(robotRepository.findById("robot-1")).thenReturn(Optional.of(existingRobot));
    when(robotStateRepository.findById("robot-1")).thenReturn(Optional.of(existingState));

    robotStateService.applyCreatedLifecycleEvent(event);

    ArgumentCaptor<Robot> robotCaptor = ArgumentCaptor.forClass(Robot.class);
    ArgumentCaptor<RobotState> stateCaptor = ArgumentCaptor.forClass(RobotState.class);
    verify(robotRepository).save(robotCaptor.capture());
    verify(robotStateRepository).save(stateCaptor.capture());
    assertEquals(RobotLifecycleStatus.ACTIVE, robotCaptor.getValue().getLifecycleStatus());
    assertEquals(11.1, stateCaptor.getValue().getPositionX());
    assertEquals(22.2, stateCaptor.getValue().getPositionY());
    assertEquals(77.7, stateCaptor.getValue().getBattery());
    assertEquals("IDLE", stateCaptor.getValue().getStatus());
    assertEquals(now, stateCaptor.getValue().getTimestamp());
  }

  @Test
  void shouldCreateRobotStateWhenMissingAndRobotExistsForCreatedEvent() {
    Instant now = Instant.parse("2026-05-19T16:40:03Z");
    RobotLifecycleEvent event = RobotLifecycleEvent.builder()
        .robotId("robot-2")
        .positionX(4.4)
        .positionY(5.5)
        .battery(6.6)
        .status("CHARGING")
        .eventType(LifecycleEventType.CREATED)
        .timestamp(now)
        .build();
    Robot existingRobot = Robot.builder()
        .robotId("robot-2")
        .displayName("Beta")
        .lifecycleStatus(RobotLifecycleStatus.CREATE_PENDING)
        .build();
    when(robotRepository.findById("robot-2")).thenReturn(Optional.of(existingRobot));
    when(robotStateRepository.findById("robot-2")).thenReturn(Optional.empty());

    robotStateService.applyCreatedLifecycleEvent(event);

    ArgumentCaptor<RobotState> stateCaptor = ArgumentCaptor.forClass(RobotState.class);
    verify(robotStateRepository).save(stateCaptor.capture());
    assertEquals("robot-2", stateCaptor.getValue().getRobotId());
    assertEquals(4.4, stateCaptor.getValue().getPositionX());
    assertEquals(5.5, stateCaptor.getValue().getPositionY());
    assertEquals(6.6, stateCaptor.getValue().getBattery());
    assertEquals("CHARGING", stateCaptor.getValue().getStatus());
    assertEquals(now, stateCaptor.getValue().getTimestamp());
  }
}
