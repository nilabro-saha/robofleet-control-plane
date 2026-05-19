package com.robofleet.fleetstateservice.robot.api;

import com.robofleet.fleetstateservice.robot.application.RobotStateService;
import com.robofleet.fleetstateservice.robot.application.dto.RobotStateResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/robots")
@RequiredArgsConstructor
public class RobotApiController {

    private final RobotStateService robotStateService;

    @GetMapping
    public List<RobotStateResponse> getAllRobots() {
        return robotStateService.getAllRobots();
    }

    @GetMapping("/{id}")
    public ResponseEntity<RobotStateResponse> getRobotById(@PathVariable("id") String id) {
        return robotStateService.getRobotById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
}
