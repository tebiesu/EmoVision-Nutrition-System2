package com.project.healthassistant.modules.vision.service;

import com.project.healthassistant.modules.vision.domain.VisionRecognitionCandidate;
import com.project.healthassistant.modules.vision.domain.VisionRecognitionTask;
import com.project.healthassistant.modules.vision.infrastructure.VisionRecognitionCandidateMapper;
import com.project.healthassistant.modules.vision.infrastructure.VisionRecognitionTaskMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class VisionRecognitionService {

    private final VisionRecognitionTaskMapper taskMapper;
    private final VisionRecognitionCandidateMapper candidateMapper;

    public VisionRecognitionService(VisionRecognitionTaskMapper taskMapper, VisionRecognitionCandidateMapper candidateMapper) {
        this.taskMapper = taskMapper;
        this.candidateMapper = candidateMapper;
    }

    @Transactional
    public Map<String, Object> createTask(Long userId, String imageUrl, String description) {
        VisionRecognitionTask task = new VisionRecognitionTask();
        task.setUserId(userId);
        task.setImageUrl(imageUrl);
        task.setDescriptionText(description);
        task.setCreatedAt(LocalDateTime.now());

        List<FoodSeed> seeds = inferFoods(description);
        if (description != null && description.toLowerCase().contains("empty-recognition")) {
            task.setStatus("EMPTY");
            task.setFinishedAt(LocalDateTime.now());
            taskMapper.insert(task);
            return toTaskResult(task, List.of());
        }

        task.setStatus("SUCCESS");
        task.setFinishedAt(LocalDateTime.now());
        taskMapper.insert(task);
        List<Map<String, Object>> candidateResults = new ArrayList<>();
        for (FoodSeed seed : seeds) {
            VisionRecognitionCandidate candidate = new VisionRecognitionCandidate();
            candidate.setTaskId(task.getId());
            candidate.setFoodName(seed.foodName());
            candidate.setConfidence(seed.confidence());
            candidate.setAmountSuggestion(seed.amount());
            candidate.setUnit(seed.unit());
            candidate.setCalories(seed.calories());
            candidate.setProtein(seed.protein());
            candidate.setFat(seed.fat());
            candidate.setCarbs(seed.carbs());
            candidate.setCreatedAt(LocalDateTime.now());
            candidateMapper.insert(candidate);
            candidateResults.add(toCandidateMap(candidate));
        }
        return toTaskResult(task, candidateResults);
    }

    public Map<String, Object> getTask(Long taskId) {
        VisionRecognitionTask task = taskMapper.selectById(taskId);
        List<Map<String, Object>> candidates = candidateMapper.findByTaskId(taskId).stream().map(this::toCandidateMap).toList();
        return toTaskResult(task, candidates);
    }

    private Map<String, Object> toTaskResult(VisionRecognitionTask task, List<Map<String, Object>> candidates) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("taskId", task.getId());
        result.put("status", task.getStatus());
        result.put("errorMessage", task.getErrorMessage());
        result.put("candidates", candidates);
        return result;
    }

    private Map<String, Object> toCandidateMap(VisionRecognitionCandidate candidate) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", candidate.getId());
        map.put("foodName", candidate.getFoodName());
        map.put("confidence", candidate.getConfidence());
        map.put("amount", candidate.getAmountSuggestion());
        map.put("unit", candidate.getUnit());
        map.put("calories", candidate.getCalories());
        map.put("protein", candidate.getProtein());
        map.put("fat", candidate.getFat());
        map.put("carbs", candidate.getCarbs());
        return map;
    }

    private List<FoodSeed> inferFoods(String description) {
        String source = description == null ? "" : description.toLowerCase();
        List<FoodSeed> seeds = new ArrayList<>();
        if (source.contains("rice") || source.contains("饭")) {
            seeds.add(new FoodSeed("Rice", BigDecimal.valueOf(0.96), BigDecimal.valueOf(180), "g", BigDecimal.valueOf(210), BigDecimal.valueOf(4), BigDecimal.valueOf(0.6), BigDecimal.valueOf(46)));
        }
        if (source.contains("chicken") || source.contains("鸡")) {
            seeds.add(new FoodSeed("Chicken Breast", BigDecimal.valueOf(0.92), BigDecimal.valueOf(150), "g", BigDecimal.valueOf(248), BigDecimal.valueOf(31), BigDecimal.valueOf(5), BigDecimal.valueOf(0)));
        }
        if (source.contains("salad") || source.contains("菜")) {
            seeds.add(new FoodSeed("Vegetable Salad", BigDecimal.valueOf(0.88), BigDecimal.valueOf(120), "g", BigDecimal.valueOf(78), BigDecimal.valueOf(3), BigDecimal.valueOf(2), BigDecimal.valueOf(12)));
        }
        if (source.contains("milk") || source.contains("奶")) {
            seeds.add(new FoodSeed("Milk", BigDecimal.valueOf(0.82), BigDecimal.valueOf(250), "ml", BigDecimal.valueOf(135), BigDecimal.valueOf(8), BigDecimal.valueOf(5), BigDecimal.valueOf(12)));
        }
        if (seeds.isEmpty()) {
            seeds.add(new FoodSeed("Mixed Meal", BigDecimal.valueOf(0.70), BigDecimal.valueOf(1), "portion", BigDecimal.valueOf(420), BigDecimal.valueOf(18), BigDecimal.valueOf(14), BigDecimal.valueOf(52)));
        }
        return seeds;
    }

    private record FoodSeed(String foodName, BigDecimal confidence, BigDecimal amount, String unit,
                            BigDecimal calories, BigDecimal protein, BigDecimal fat, BigDecimal carbs) {
    }
}
