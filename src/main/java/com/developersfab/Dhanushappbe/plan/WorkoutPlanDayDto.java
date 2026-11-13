package com.developersfab.Dhanushappbe.plan;

import java.util.List;

public record WorkoutPlanDayDto(
        Integer day,
        List<String> categories,
        List<WorkoutPlanEntryDto> entries
) {
}

