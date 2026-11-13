package com.developersfab.Dhanushappbe.plan;

public record DashboardResponse(
        Integer day,
        WorkoutPlanDayDto workout,
        FoodPlanDayDto meals
) {
}

