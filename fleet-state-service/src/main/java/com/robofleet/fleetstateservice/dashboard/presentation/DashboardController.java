package com.robofleet.fleetstateservice.dashboard.presentation;

import com.robofleet.fleetstateservice.robot.application.RobotStateService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final RobotStateService robotStateService;

    @GetMapping("/")
    public String dashboard(Model model) {
        model.addAttribute("robots", robotStateService.getAllRobots());
        return "dashboard";
    }
}
