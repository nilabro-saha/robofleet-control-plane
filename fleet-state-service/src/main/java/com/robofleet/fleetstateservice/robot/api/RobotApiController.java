package com.robofleet.fleetstateservice.robot.api;

import com.robofleet.fleetstateservice.robot.application.RobotStateService;
import com.robofleet.fleetstateservice.robot.application.dto.CreateRobotRequest;
import com.robofleet.fleetstateservice.robot.application.dto.RobotCreationResponse;
import com.robofleet.fleetstateservice.robot.application.dto.RobotStateResponse;
import com.robofleet.fleetstateservice.robot.application.dto.RobotSummaryResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Read-facing API for current fleet state.
 *
 * <p>Intent: expose a clean, stable query surface for operator experiences
 * (dashboards, status widgets, ad-hoc checks) without coupling them to Kafka
 * or persistence implementation details.</p>
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
   */
  @GetMapping("/robots")
  public List<RobotSummaryResponse> getAllRobots(
      @PageableDefault(size = 100, sort = "robotId", direction = Sort.Direction.ASC)
      Pageable pageable) {
    return robotStateService.getAllRobots(pageable);
  }

  /**
   * Fetches robot identity/lifecycle data for one robot.
   */
  @GetMapping("/robots/{id}")
  public ResponseEntity<RobotSummaryResponse> getRobotById(@PathVariable("id") String id) {
    return robotStateService.getRobotById(id)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  /**
   * Creates a new robot orchestration request and marks it pending.
   */
  @PostMapping("/robots")
  public ResponseEntity<RobotCreationResponse> createRobot(
      @RequestBody CreateRobotRequest request) {
    RobotCreationResponse response = robotStateService.createRobot(request);
    return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
  }
}
