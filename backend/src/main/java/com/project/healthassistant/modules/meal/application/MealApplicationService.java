package com.project.healthassistant.modules.meal.application;

import com.project.healthassistant.common.api.ResultCode;
import com.project.healthassistant.common.exception.BusinessException;
import com.project.healthassistant.modules.meal.domain.*;
import com.project.healthassistant.modules.meal.infrastructure.*;
import com.project.healthassistant.modules.profile.application.ProfileApplicationService;
import com.project.healthassistant.modules.profile.domain.UserProfile;
import com.project.healthassistant.modules.vision.domain.VisionRecognitionCandidate;
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
            throw new BusinessException(ResultCode.INVALID_PARAM.getCode(), "Meal items are required");
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
            riskTags.add("HIGH_CALORIE");
        }
        if (totalProtein.compareTo(BigDecimal.valueOf(20)) < 0) {
            penalty += 15;
            riskTags.add("LOW_PROTEIN");
        }
        if (totalFat.compareTo(BigDecimal.valueOf(35)) > 0) {
            penalty += 15;
            riskTags.add("HIGH_FAT");
        }
        if (totalCarbs.compareTo(BigDecimal.valueOf(90)) > 0) {
            penalty += 15;
            riskTags.add("HIGH_CARB");
        }
        if (profile.getGoal() != null && profile.getGoal().toLowerCase().contains("loss") && totalCalories.compareTo(BigDecimal.valueOf(700)) > 0) {
            penalty += 10;
            riskTags.add("GOAL_MISMATCH");
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
        assessment.setEvidenceText(String.format("Calories=%s, Protein=%s, Fat=%s, Carbs=%s", totalCalories, totalProtein, totalFat, totalCarbs));
        assessment.setCreatedAt(LocalDateTime.now());
        return assessment;
    }

    private RecommendationRecord buildRecommendation(Long mealRecordId, NutritionAssessment assessment, EmotionOutcome emotionOutcome,
                                                     UserProfile profile, String emotionText) {
        boolean forceFallback = emotionText != null && emotionText.toLowerCase().contains("fallback-ai");
        String summary = switch (assessment.getRiskLevel()) {
            case "HIGH" -> "This meal needs immediate adjustment";
            case "MEDIUM" -> "This meal is acceptable but can be improved";
            default -> "This meal is balanced overall";
        };
        String recommendationText;
        int aiEnhanced;
        int fallbackMode;
        if (forceFallback) {
            aiEnhanced = 0;
            fallbackMode = 1;
            recommendationText = summary + ". Focus on lighter cooking, more vegetables, and sufficient hydration.";
        } else {
            aiEnhanced = 1;
            fallbackMode = 0;
            recommendationText = summary + ". Based on your " + profile.getGoal() + " goal and current mood of " + emotionOutcome.label()
                    + ", consider pairing the next meal with high-fiber vegetables and lean protein.";
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
            return new EmotionOutcome("SELF_REPORTED", BigDecimal.valueOf(selfRating));
        }
        String lower = emotionText == null ? "" : emotionText.toLowerCase();
        if (lower.contains("stress") || lower.contains("sad") || lower.contains("anxious")) {
            return new EmotionOutcome("NEGATIVE", BigDecimal.valueOf(Math.max(1, selfRating - 1)));
        }
        if (lower.contains("happy") || lower.contains("calm") || lower.contains("relaxed")) {
            return new EmotionOutcome("POSITIVE", BigDecimal.valueOf(Math.min(5, selfRating + 0.5)));
        }
        return new EmotionOutcome("NEUTRAL", BigDecimal.valueOf(selfRating));
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
        summary.setSummaryText("Today you consumed " + totalCalories + " kcal with mood score " + avgEmotion + ".");
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
