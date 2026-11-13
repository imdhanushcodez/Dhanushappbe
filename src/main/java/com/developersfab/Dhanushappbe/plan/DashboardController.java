package com.developersfab.Dhanushappbe.plan;

import com.developersfab.Dhanushappbe.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final PlanService planService;

    @GetMapping
    public ResponseEntity<DashboardResponse> getDashboard(@RequestParam int day) {
        Long userId = SecurityUtils.getCurrentUserId();
        DashboardResponse response = planService.getDashboard(userId, day);
        return ResponseEntity.ok(response);
    }
}

