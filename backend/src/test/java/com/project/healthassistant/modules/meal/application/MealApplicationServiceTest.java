package com.project.healthassistant.modules.meal.application;

import com.project.healthassistant.modules.meal.domain.*;
import com.project.healthassistant.modules.meal.infrastructure.*;
import com.project.healthassistant.modules.profile.application.ProfileApplicationService;
import com.project.healthassistant.modules.profile.domain.UserProfile;
import com.project.healthassistant.modules.vision.domain.VisionRecognitionCandidate;
import com.project.healthassistant.modules.vision.infrastructure.VisionRecognitionCandidateMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MealApplicationServiceTest {

    @Mock private MealRecordMapper mealRecordMapper;
    @Mock private MealItemMapper mealItemMapper;
    @Mock private EmotionRecordMapper emotionRecordMapper;
    @Mock private NutritionAssessmentMapper nutritionAssessmentMapper;
    @Mock private RecommendationRecordMapper recommendationRecordMapper;
    @Mock private DailyHealthSummaryMapper dailyHealthSummaryMapper;
    @Mock private VisionRecognitionCandidateMapper visionRecognitionCandidateMapper;
    @Mock private ProfileApplicationService profileApplicationService;

    private MealApplicationService mealApplicationService;

    @BeforeEach
    void setUp() {
        mealApplicationService = new MealApplicationService(mealRecordMapper, mealItemMapper, emotionRecordMapper,
                nutritionAssessmentMapper, recommendationRecordMapper, dailyHealthSummaryMapper,
                visionRecognitionCandidateMapper, profileApplicationService);
    }

    @Test
    void shouldCreateMealUsingRecognizedItemsAndFallbackRecommendation() {
        UserProfile profile = new UserProfile();
        profile.setGoal("fat-loss");
        when(profileApplicationService.requireProfile(1L)).thenReturn(profile);
        doAnswer(invocation -> {
            MealRecord record = invocation.getArgument(0);
            record.setId(11L);
            return 1;
        }).when(mealRecordMapper).insert(any(MealRecord.class));
        when(visionRecognitionCandidateMapper.findByTaskId(7L)).thenReturn(List.of(candidate("Rice", 220), candidate("Chicken Breast", 250)));
        when(mealRecordMapper.findByUserId(1L)).thenReturn(List.of());
        when(dailyHealthSummaryMapper.findByUserIdAndDate(any(), any())).thenReturn(null);

        Map<String, Object> meal = mealApplicationService.createMeal(1L,
                new MealApplicationService.CreateMealCommand("lunch", LocalDateTime.now(), "chicken rice", "/uploads/a.png", 7L, 3, "fallback-ai", List.of()));

        Map<String, Object> recommendation = (Map<String, Object>) meal.get("recommendation");
        assertEquals(true, recommendation.get("fallbackMode"));
    }

    private VisionRecognitionCandidate candidate(String foodName, int calories) {
        VisionRecognitionCandidate candidate = new VisionRecognitionCandidate();
        candidate.setFoodName(foodName);
        candidate.setAmountSuggestion(BigDecimal.valueOf(100));
        candidate.setUnit("g");
        candidate.setCalories(BigDecimal.valueOf(calories));
        candidate.setProtein(BigDecimal.valueOf(15));
        candidate.setFat(BigDecimal.valueOf(5));
        candidate.setCarbs(BigDecimal.valueOf(20));
        return candidate;
    }
}
