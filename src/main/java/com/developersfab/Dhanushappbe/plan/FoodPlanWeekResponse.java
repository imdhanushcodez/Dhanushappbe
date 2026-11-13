package com.developersfab.Dhanushappbe.plan;

import java.util.List;

public record FoodPlanWeekResponse(
        List<FoodPlanDayDto> days
) {
}

