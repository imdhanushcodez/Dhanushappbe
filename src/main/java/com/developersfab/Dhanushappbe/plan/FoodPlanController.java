package com.developersfab.Dhanushappbe.plan;

import com.developersfab.Dhanushappbe.security.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/plans/foods")
@RequiredArgsConstructor
public class FoodPlanController {

    private final PlanService planService;

    @GetMapping
    public ResponseEntity<FoodPlanWeekResponse> getFoodPlan() {
        Long userId = SecurityUtils.getCurrentUserId();
        FoodPlanWeekResponse response = planService.getFoodWeekPlan(userId);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<FoodPlanEntryDto> addFoodToDay(@Valid @RequestBody AddFoodToDayRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        FoodPlanEntryDto created = planService.addFoodToDay(userId, request);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @DeleteMapping("/{entryId}")
    public ResponseEntity<Void> removeFoodFromDay(@PathVariable Long entryId) {
        Long userId = SecurityUtils.getCurrentUserId();
        planService.removeFoodFromDay(userId, entryId);
        return ResponseEntity.noContent().build();
    }
}

