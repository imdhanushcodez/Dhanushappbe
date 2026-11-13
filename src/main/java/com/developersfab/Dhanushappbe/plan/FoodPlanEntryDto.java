package com.developersfab.Dhanushappbe.plan;

import java.math.BigDecimal;

public record FoodPlanEntryDto(
        Long entryId,
        Integer day,
        Integer position,
        Long foodId,
        String dishName,
        String category,
        BigDecimal weightG,
        BigDecimal calories,
        BigDecimal proteinG,
        BigDecimal fiberG,
        String imageUrl
) {
}

