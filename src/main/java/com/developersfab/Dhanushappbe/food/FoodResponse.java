package com.developersfab.Dhanushappbe.food;

import com.developersfab.Dhanushappbe.common.CategoryDto;

import java.math.BigDecimal;
import java.time.Instant;

public record FoodResponse(
        Long id,
        CategoryDto category,
        String dishName,
        BigDecimal weightG,
        BigDecimal calories,
        BigDecimal proteinG,
        BigDecimal fiberG,
        String imageUrl,
        String others,
        Instant createdAt,
        Instant updatedAt
) {
}

