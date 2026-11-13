package com.developersfab.Dhanushappbe.plan;

import java.math.BigDecimal;
import java.util.List;

public record FoodPlanDayDto(
        Integer day,
        BigDecimal totalCalories,
        BigDecimal totalProtein,
        List<FoodPlanEntryDto> entries
) {
}

