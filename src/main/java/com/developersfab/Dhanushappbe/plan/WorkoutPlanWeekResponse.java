package com.developersfab.Dhanushappbe.plan;

import java.util.List;

public record WorkoutPlanWeekResponse(
        List<WorkoutPlanDayDto> days
) {
}

