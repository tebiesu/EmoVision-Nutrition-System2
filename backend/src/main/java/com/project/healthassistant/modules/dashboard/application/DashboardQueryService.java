package com.project.healthassistant.modules.dashboard.application;

import com.project.healthassistant.modules.meal.application.MealApplicationService;
import com.project.healthassistant.modules.meal.domain.DailyHealthSummary;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class DashboardQueryService {

    private final MealApplicationService mealApplicationService;

    public DashboardQueryService(MealApplicationService mealApplicationService) {
        this.mealApplicationService = mealApplicationService;
    }

    public Map<String, Object> getSummary(Long userId) {
        List<Map<String, Object>> meals = mealApplicationService.listMeals(userId);
        Map<String, Object> latestMeal = meals.isEmpty() ? Map.of() : meals.get(0);
        DailyHealthSummary dailySummary = mealApplicationService.latestDailySummary(userId);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("todayMealCount", meals.size());
        result.put("latestMeal", latestMeal);
        result.put("dailySummary", dailySummary == null ? Map.of(
                "summaryText", "No meal recorded today",
                "totalCalories", 0,
                "avgEmotionScore", 0
        ) : Map.of(
                "summaryText", dailySummary.getSummaryText(),
                "totalCalories", dailySummary.getTotalCalories(),
                "avgEmotionScore", dailySummary.getAvgEmotionScore()
        ));
        return result;
    }
}
