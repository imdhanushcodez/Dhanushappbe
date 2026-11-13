package com.developersfab.Dhanushappbe.plan;

import java.math.BigDecimal;

public record WorkoutPlanEntryDto(
        Long entryId,
        Integer day,
        Integer position,
        Long workoutId,
        String name,
        String category,
        Integer reps,
        BigDecimal weightKg,
        String imageUrl
) {
}

