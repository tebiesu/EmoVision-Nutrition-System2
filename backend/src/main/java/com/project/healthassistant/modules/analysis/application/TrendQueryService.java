package com.project.healthassistant.modules.analysis.application;

import com.project.healthassistant.modules.meal.application.MealApplicationService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class TrendQueryService {

    private final MealApplicationService mealApplicationService;

    public TrendQueryService(MealApplicationService mealApplicationService) {
        this.mealApplicationService = mealApplicationService;
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> getTrends(Long userId) {
        List<Map<String, Object>> meals = mealApplicationService.listMeals(userId);
        List<Map<String, Object>> calorieTrend = meals.stream().map(meal -> Map.of(
                "date", meal.get("date"),
                "value", ((Map<String, Object>) meal.get("assessment")).getOrDefault("totalCalories", 0)
        )).toList();
        List<Map<String, Object>> emotionTrend = meals.stream().map(meal -> Map.of(
                "date", meal.get("date"),
                "value", ((Map<String, Object>) meal.get("emotion")).getOrDefault("sentimentScore", 0)
        )).toList();

        BigDecimal totalProtein = BigDecimal.ZERO;
        BigDecimal totalFat = BigDecimal.ZERO;
        BigDecimal totalCarbs = BigDecimal.ZERO;
        for (Map<String, Object> meal : meals) {
            Map<String, Object> assessment = (Map<String, Object>) meal.get("assessment");
            totalProtein = totalProtein.add(new BigDecimal(String.valueOf(assessment.getOrDefault("totalProtein", 0))));
            totalFat = totalFat.add(new BigDecimal(String.valueOf(assessment.getOrDefault("totalFat", 0))));
            totalCarbs = totalCarbs.add(new BigDecimal(String.valueOf(assessment.getOrDefault("totalCarbs", 0))));
        }
        Map<String, Object> radar = new LinkedHashMap<>();
        radar.put("protein", totalProtein);
        radar.put("fat", totalFat);
        radar.put("carbs", totalCarbs);
        radar.put("balance", meals.isEmpty() ? 0 : 100 - Math.min(70, meals.size() * 5));

        List<Map<String, Object>> correlation = meals.stream().map(meal -> Map.of(
                "emotionScore", ((Map<String, Object>) meal.get("emotion")).getOrDefault("sentimentScore", 0),
                "calories", ((Map<String, Object>) meal.get("assessment")).getOrDefault("totalCalories", 0)
        )).toList();

        return Map.of(
                "calorieTrend", calorieTrend,
                "emotionTrend", emotionTrend,
                "nutritionRadar", radar,
                "correlation", correlation,
                "hasEnoughData", meals.size() >= 2
        );
    }
}
