package com.project.healthassistant.modules.meal.application;

import com.project.healthassistant.common.api.ResultCode;
import com.project.healthassistant.common.exception.BusinessException;
import com.project.healthassistant.modules.meal.domain.DailyHealthSummary;
import com.project.healthassistant.modules.meal.domain.EmotionRecord;
import com.project.healthassistant.modules.meal.domain.MealItem;
import com.project.healthassistant.modules.meal.domain.MealRecord;
import com.project.healthassistant.modules.meal.domain.NutritionAssessment;
import com.project.healthassistant.modules.meal.domain.RecommendationRecord;
import com.project.healthassistant.modules.meal.infrastructure.DailyHealthSummaryMapper;
import com.project.healthassistant.modules.meal.infrastructure.EmotionRecordMapper;
import com.project.healthassistant.modules.meal.infrastructure.MealItemMapper;
import com.project.healthassistant.modules.meal.infrastructure.MealRecordMapper;
import com.project.healthassistant.modules.meal.infrastructure.NutritionAssessmentMapper;
import com.project.healthassistant.modules.meal.infrastructure.RecommendationRecordMapper;
import com.project.healthassistant.modules.profile.application.ProfileApplicationService;
import com.project.healthassistant.modules.profile.domain.UserProfile;
import com.project.healthassistant.modules.vision.infrastructure.VisionRecognitionCandidateMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class MealApplicationService {

    private final MealRecordMapper mealRecordMapper;
    private final MealItemMapper mealItemMapper;
    private final EmotionRecordMapper emotionRecordMapper;
    private final NutritionAssessmentMapper nutritionAssessmentMapper;
    private final RecommendationRecordMapper recommendationRecordMapper;
    private final DailyHealthSummaryMapper dailyHealthSummaryMapper;
    private final VisionRecognitionCandidateMapper visionRecognitionCandidateMapper;
    private final ProfileApplicationService profileApplicationService;

    public MealApplicationService(MealRecordMapper mealRecordMapper,
                                  MealItemMapper mealItemMapper,
                                  EmotionRecordMapper emotionRecordMapper,
                                  NutritionAssessmentMapper nutritionAssessmentMapper,
                                  RecommendationRecordMapper recommendationRecordMapper,
                                  DailyHealthSummaryMapper dailyHealthSummaryMapper,
                                  VisionRecognitionCandidateMapper visionRecognitionCandidateMapper,
                                  ProfileApplicationService profileApplicationService) {
        this.mealRecordMapper = mealRecordMapper;
        this.mealItemMapper = mealItemMapper;
        this.emotionRecordMapper = emotionRecordMapper;
        this.nutritionAssessmentMapper = nutritionAssessmentMapper;
        this.recommendationRecordMapper = recommendationRecordMapper;
        this.dailyHealthSummaryMapper = dailyHealthSummaryMapper;
        this.visionRecognitionCandidateMapper = visionRecognitionCandidateMapper;
        this.profileApplicationService = profileApplicationService;
    }

    @Transactional
    public Map<String, Object> createMeal(Long userId, CreateMealCommand command) {
        UserProfile profile = profileApplicationService.requireProfile(userId);

        MealRecord record = new MealRecord();
        record.setUserId(userId);
        record.setMealType(command.mealType());
        record.setEatenAt(command.eatenAt());
        record.setDescriptionText(command.description());
        record.setImageUrl(command.imageUrl());
        record.setRecognitionTaskId(command.recognitionTaskId());
        record.setCreatedAt(LocalDateTime.now());
        record.setUpdatedAt(LocalDateTime.now());
        mealRecordMapper.insert(record);

        List<ItemCommand> itemCommands = new ArrayList<>(command.items());
        if (itemCommands.isEmpty() && command.recognitionTaskId() != null) {
            itemCommands = visionRecognitionCandidateMapper.findByTaskId(command.recognitionTaskId()).stream()
                    .map(candidate -> new ItemCommand(candidate.getFoodName(), candidate.getAmountSuggestion(), candidate.getUnit(),
                            candidate.getCalories(), candidate.getProtein(), candidate.getFat(), candidate.getCarbs(), "VISION", true))
                    .collect(Collectors.toList());
        }
        if (itemCommands.isEmpty()) {
            throw new BusinessException(ResultCode.INVALID_PARAM.getCode(), "\u8bf7\u81f3\u5c11\u5f55\u5165\u4e00\u79cd\u98df\u7269");
        }

        List<MealItem> savedItems = new ArrayList<>();
        for (ItemCommand item : itemCommands) {
            MealItem entity = new MealItem();
            entity.setMealRecordId(record.getId());
            entity.setFoodName(item.foodName());
            entity.setAmount(item.amount());
            entity.setUnit(item.unit());
            entity.setCalories(item.calories());
            entity.setProtein(item.protein());
            entity.setFat(item.fat());
            entity.setCarbs(item.carbs());
            entity.setSource(item.source());
            entity.setConfirmed(item.confirmed() ? 1 : 0);
            entity.setCreatedAt(LocalDateTime.now());
            mealItemMapper.insert(entity);
            savedItems.add(entity);
        }

        EmotionOutcome emotionOutcome = analyzeEmotion(command.selfRating(), command.emotionText());
        EmotionRecord emotionRecord = new EmotionRecord();
        emotionRecord.setMealRecordId(record.getId());
        emotionRecord.setSelfRating(command.selfRating());
        emotionRecord.setTextContent(command.emotionText());
        emotionRecord.setSentimentLabel(emotionOutcome.label());
        emotionRecord.setSentimentScore(emotionOutcome.score());
        emotionRecord.setCreatedAt(LocalDateTime.now());
        emotionRecordMapper.insert(emotionRecord);

        NutritionAssessment assessment = buildAssessment(record.getId(), savedItems, profile);
        nutritionAssessmentMapper.insert(assessment);

        RecommendationRecord recommendation = buildRecommendation(record.getId(), assessment, emotionOutcome, profile, command.emotionText());
        recommendationRecordMapper.insert(recommendation);

        refreshDailySummary(userId, record.getEatenAt().toLocalDate());

        return buildMealResponse(record, savedItems, emotionRecord, assessment, recommendation);
    }

    public List<Map<String, Object>> listMeals(Long userId) {
        return mealRecordMapper.findByUserId(userId).stream()
                .map(record -> buildMealResponse(
                        record,
                        mealItemMapper.findByMealRecordId(record.getId()),
                        emotionRecordMapper.findByMealRecordId(record.getId()),
                        nutritionAssessmentMapper.findByMealRecordId(record.getId()),
                        recommendationRecordMapper.findByMealRecordId(record.getId())
                ))
                .toList();
    }

    public DailyHealthSummary latestDailySummary(Long userId) {
        return dailyHealthSummaryMapper.findByUserIdAndDate(userId, LocalDate.now());
    }

    private NutritionAssessment buildAssessment(Long mealRecordId, List<MealItem> items, UserProfile profile) {
        BigDecimal totalCalories = items.stream().map(MealItem::getCalories).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalProtein = items.stream().map(MealItem::getProtein).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalFat = items.stream().map(MealItem::getFat).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalCarbs = items.stream().map(MealItem::getCarbs).reduce(BigDecimal.ZERO, BigDecimal::add);

        int penalty = 0;
        List<String> riskTags = new ArrayList<>();
        if (totalCalories.compareTo(BigDecimal.valueOf(850)) > 0) {
            penalty += 25;
            riskTags.add("\u70ed\u91cf\u504f\u9ad8");
        }
        if (totalProtein.compareTo(BigDecimal.valueOf(20)) < 0) {
            penalty += 15;
            riskTags.add("\u86cb\u767d\u8d28\u4e0d\u8db3");
        }
        if (totalFat.compareTo(BigDecimal.valueOf(35)) > 0) {
            penalty += 15;
            riskTags.add("\u8102\u80aa\u504f\u9ad8");
        }
        if (totalCarbs.compareTo(BigDecimal.valueOf(90)) > 0) {
            penalty += 15;
            riskTags.add("\u78b3\u6c34\u504f\u9ad8");
        }
        if (profile.getGoal() != null && profile.getGoal().toLowerCase().contains("loss") && totalCalories.compareTo(BigDecimal.valueOf(700)) > 0) {
            penalty += 10;
            riskTags.add("\u4e0e\u51cf\u8102\u76ee\u6807\u4e0d\u7b26");
        }

        NutritionAssessment assessment = new NutritionAssessment();
        assessment.setMealRecordId(mealRecordId);
        assessment.setTotalCalories(totalCalories.setScale(2, RoundingMode.HALF_UP));
        assessment.setTotalProtein(totalProtein.setScale(2, RoundingMode.HALF_UP));
        assessment.setTotalFat(totalFat.setScale(2, RoundingMode.HALF_UP));
        assessment.setTotalCarbs(totalCarbs.setScale(2, RoundingMode.HALF_UP));
        assessment.setStructureScore(BigDecimal.valueOf(Math.max(30, 100 - penalty)).setScale(2, RoundingMode.HALF_UP));
        assessment.setRiskLevel(penalty >= 35 ? "HIGH" : penalty >= 20 ? "MEDIUM" : "LOW");
        assessment.setRiskTags(String.join(",", riskTags));
        assessment.setEvidenceText(String.format("\u70ed\u91cf=%s\uff0c\u86cb\u767d\u8d28=%s\uff0c\u8102\u80aa=%s\uff0c\u78b3\u6c34=%s", totalCalories, totalProtein, totalFat, totalCarbs));
        assessment.setCreatedAt(LocalDateTime.now());
        return assessment;
    }

    private RecommendationRecord buildRecommendation(Long mealRecordId, NutritionAssessment assessment, EmotionOutcome emotionOutcome,
                                                     UserProfile profile, String emotionText) {
        boolean forceFallback = emotionText != null && emotionText.toLowerCase().contains("fallback-ai");
        String summary = switch (assessment.getRiskLevel()) {
            case "HIGH" -> "\u8fd9\u9910\u9700\u8981\u5c3d\u5feb\u8c03\u6574";
            case "MEDIUM" -> "\u8fd9\u9910\u6574\u4f53\u5c1a\u53ef\uff0c\u4f46\u8fd8\u6709\u4f18\u5316\u7a7a\u95f4";
            default -> "\u8fd9\u9910\u6574\u4f53\u6bd4\u8f83\u5747\u8861";
        };
        String recommendationText;
        int aiEnhanced;
        int fallbackMode;
        String goalLabel = switch (profile.getGoal()) {
            case "fat-loss" -> "\u51cf\u8102";
            case "muscle-gain" -> "\u589e\u808c";
            default -> "\u7ef4\u6301\u72b6\u6001";
        };
        if (forceFallback) {
            aiEnhanced = 0;
            fallbackMode = 1;
            recommendationText = summary + "\u3002\u5efa\u8bae\u51cf\u5c11\u6cb9\u70b8\u548c\u9ad8\u7cd6\u642d\u914d\uff0c\u8865\u5145\u852c\u83dc\uff0c\u5e76\u6ce8\u610f\u996e\u6c34\u3002";
        } else {
            aiEnhanced = 1;
            fallbackMode = 0;
            recommendationText = summary + "\u3002\u7ed3\u5408\u4f60\u5f53\u524d\u7684" + goalLabel + "\u76ee\u6807\u548c" + emotionOutcome.label()
                    + "\u72b6\u6001\uff0c\u4e0b\u4e00\u9910\u53ef\u4f18\u5148\u642d\u914d\u9ad8\u7ea4\u7ef4\u852c\u83dc\u4e0e\u4f18\u8d28\u86cb\u767d\u3002";
        }
        RecommendationRecord record = new RecommendationRecord();
        record.setMealRecordId(mealRecordId);
        record.setAiEnhanced(aiEnhanced);
        record.setFallbackMode(fallbackMode);
        record.setSummaryText(summary);
        record.setRecommendationText(recommendationText);
        record.setCreatedAt(LocalDateTime.now());
        return record;
    }

    private EmotionOutcome analyzeEmotion(Integer selfRating, String emotionText) {
        if (emotionText != null && emotionText.toLowerCase().contains("timeout-emotion")) {
            return new EmotionOutcome("\u4ee5\u81ea\u8bc4\u4e3a\u51c6", BigDecimal.valueOf(selfRating));
        }
        String lower = emotionText == null ? "" : emotionText.toLowerCase();
        if (lower.contains("stress") || lower.contains("sad") || lower.contains("anxious") || lower.contains("\u538b\u529b") || lower.contains("\u96be\u8fc7") || lower.contains("\u7126\u8651")) {
            return new EmotionOutcome("\u60c5\u7eea\u504f\u4f4e", BigDecimal.valueOf(Math.max(1, selfRating - 1)));
        }
        if (lower.contains("happy") || lower.contains("calm") || lower.contains("relaxed") || lower.contains("\u5f00\u5fc3") || lower.contains("\u5e73\u9759") || lower.contains("\u653e\u677e")) {
            return new EmotionOutcome("\u60c5\u7eea\u7a33\u5b9a", BigDecimal.valueOf(Math.min(5, selfRating + 0.5)));
        }
        return new EmotionOutcome("\u60c5\u7eea\u4e2d\u6027", BigDecimal.valueOf(selfRating));
    }

    private void refreshDailySummary(Long userId, LocalDate date) {
        List<Map<String, Object>> meals = listMeals(userId).stream()
                .filter(item -> date.toString().equals(item.get("date")))
                .toList();
        BigDecimal totalCalories = meals.stream()
                .map(item -> new BigDecimal(String.valueOf(((Map<?, ?>) item.get("assessment")).get("totalCalories"))))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal avgEmotion = meals.isEmpty() ? BigDecimal.ZERO : meals.stream()
                .map(item -> new BigDecimal(String.valueOf(((Map<?, ?>) item.get("emotion")).get("sentimentScore"))))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(meals.size()), 2, RoundingMode.HALF_UP);

        DailyHealthSummary summary = dailyHealthSummaryMapper.findByUserIdAndDate(userId, date);
        if (summary == null) {
            summary = new DailyHealthSummary();
            summary.setUserId(userId);
            summary.setSummaryDate(date);
            summary.setCreatedAt(LocalDateTime.now());
        }
        summary.setTotalCalories(totalCalories);
        summary.setAvgEmotionScore(avgEmotion);
        summary.setSummaryText("\u4eca\u65e5\u7d2f\u8ba1\u6444\u5165 " + totalCalories + " \u5343\u5361\uff0c\u5e73\u5747\u60c5\u7eea\u5206\u4e3a " + avgEmotion + "\u3002");
        summary.setUpdatedAt(LocalDateTime.now());
        if (summary.getId() == null) {
            dailyHealthSummaryMapper.insert(summary);
        } else {
            dailyHealthSummaryMapper.updateById(summary);
        }
    }

    private Map<String, Object> buildMealResponse(MealRecord record, List<MealItem> items, EmotionRecord emotion,
                                                  NutritionAssessment assessment, RecommendationRecord recommendation) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", record.getId());
        data.put("mealType", record.getMealType());
        data.put("eatenAt", record.getEatenAt());
        data.put("date", record.getEatenAt().toLocalDate().toString());
        data.put("description", record.getDescriptionText());
        data.put("imageUrl", record.getImageUrl());
        data.put("recognitionTaskId", record.getRecognitionTaskId());
        data.put("items", items.stream().map(item -> Map.of(
                "foodName", item.getFoodName(),
                "amount", item.getAmount(),
                "unit", item.getUnit(),
                "calories", item.getCalories(),
                "protein", item.getProtein(),
                "fat", item.getFat(),
                "carbs", item.getCarbs(),
                "source", item.getSource()
        )).toList());
        data.put("emotion", emotion == null ? Map.of() : Map.of(
                "selfRating", emotion.getSelfRating(),
                "textContent", emotion.getTextContent() == null ? "" : emotion.getTextContent(),
                "sentimentLabel", emotion.getSentimentLabel(),
                "sentimentScore", emotion.getSentimentScore()
        ));
        data.put("assessment", assessment == null ? Map.of() : Map.of(
                "totalCalories", assessment.getTotalCalories(),
                "totalProtein", assessment.getTotalProtein(),
                "totalFat", assessment.getTotalFat(),
                "totalCarbs", assessment.getTotalCarbs(),
                "structureScore", assessment.getStructureScore(),
                "riskLevel", assessment.getRiskLevel(),
                "riskTags", assessment.getRiskTags() == null || assessment.getRiskTags().isBlank() ? List.of() : List.of(assessment.getRiskTags().split(",")),
                "evidenceText", assessment.getEvidenceText()
        ));
        data.put("recommendation", recommendation == null ? Map.of() : Map.of(
                "aiEnhanced", recommendation.getAiEnhanced() == 1,
                "fallbackMode", recommendation.getFallbackMode() == 1,
                "summaryText", recommendation.getSummaryText(),
                "recommendationText", recommendation.getRecommendationText()
        ));
        return data;
    }

    public record CreateMealCommand(
            String mealType,
            LocalDateTime eatenAt,
            String description,
            String imageUrl,
            Long recognitionTaskId,
            Integer selfRating,
            String emotionText,
            List<ItemCommand> items
    ) {}

    public record ItemCommand(
            String foodName,
            BigDecimal amount,
            String unit,
            BigDecimal calories,
            BigDecimal protein,
            BigDecimal fat,
            BigDecimal carbs,
            String source,
            boolean confirmed
    ) {}

    private record EmotionOutcome(String label, BigDecimal score) {}
}
