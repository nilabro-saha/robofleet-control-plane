package com.robofleet.fleetstateservice.robot.api;

import com.robofleet.fleetstateservice.robot.application.RobotStateService;
import com.robofleet.fleetstateservice.robot.application.dto.CreateRobotRequest;
import com.robofleet.fleetstateservice.robot.application.dto.RobotCreationResponse;
import com.robofleet.fleetstateservice.robot.application.dto.RobotStateResponse;
import com.robofleet.fleetstateservice.robot.application.dto.RobotSummaryResponse;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Read-facing API for current fleet state.
 *
 * <p>Intent: expose a clean, stable query surface for operator experiences
 * (dashboards, status widgets, ad-hoc checks) without coupling them to Kafka
 * or persistence implementation details.</p>
 *
 * @author Nilabro Saha
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class RobotApiController {

  private final RobotStateService robotStateService;

  /**
   * Lists the latest known state for all robots.
   *
   * @param pageable pagination and sorting request
   * @return current fleet snapshot
   */
  @GetMapping("/robot-statuses")
  public List<RobotStateResponse> getAllRobotStatuses(
      @PageableDefault(size = 100, sort = "robotId", direction = Sort.Direction.ASC)
      Pageable pageable) {
    return robotStateService.getAllRobotStatuses(pageable);
  }

  /**
   * Fetches the latest known state for a specific robot.
   *
   * @param id robot identifier
   * @return 200 with state when found, otherwise 404
   */
  @GetMapping("/robot-statuses/{id}")
  public ResponseEntity<RobotStateResponse> getRobotStatusById(@PathVariable("id") String id) {
    return robotStateService.getRobotStatusById(id)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  /**
   * Lists robot identity/lifecycle data.
   *
   * @param pageable pagination and sorting request
   * @return robot identity/lifecycle projection list
   */
  @GetMapping("/robots")
  public List<RobotSummaryResponse> getAllRobots(
      @PageableDefault(size = 100, sort = "robotId", direction = Sort.Direction.ASC)
      Pageable pageable) {
    return robotStateService.getAllRobots(pageable);
  }

  /**
   * Fetches robot identity/lifecycle data for one robot.
   *
   * @param id robot identifier
   * @return 200 with summary when found, otherwise 404
   */
  @GetMapping("/robots/{id}")
  public ResponseEntity<RobotSummaryResponse> getRobotById(@PathVariable("id") String id) {
    return robotStateService.getRobotById(id)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  /**
   * Creates a new robot orchestration request and marks it pending.
   *
   * @param request create-robot request payload
   * @return 202 response containing pending robot metadata
   */
  @PostMapping("/robots")
  public ResponseEntity<RobotCreationResponse> createRobot(
      @RequestBody CreateRobotRequest request) {
    RobotCreationResponse response = robotStateService.createRobot(request);
    return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
  }

  /**
   * Requests asynchronous robot deletion via lifecycle workflow.
   *
   * @param id robot identifier
   * @param correlationId optional correlation identifier from request header
   * @return 202 with pending-delete summary when found, otherwise 404
   */
  @DeleteMapping("/robots/{id}")
  public ResponseEntity<RobotSummaryResponse> deleteRobot(
      @PathVariable("id") String id,
      @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId) {
    String effectiveCorrelationId = correlationId == null || correlationId.isBlank()
        ? UUID.randomUUID().toString()
        : correlationId;
    return robotStateService.requestRobotDeletion(id, effectiveCorrelationId)
        .map(summary -> ResponseEntity.status(HttpStatus.ACCEPTED).body(summary))
        .orElse(ResponseEntity.notFound().build());
  }
}
