package com.project.healthassistant.modules.meal.controller;

import com.project.healthassistant.common.api.ApiResponse;
import com.project.healthassistant.common.security.CurrentUserService;
import com.project.healthassistant.common.trace.TraceIdHolder;
import com.project.healthassistant.modules.meal.application.MealApplicationService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Validated
@RestController
@RequestMapping("/api/v1/meals")
public class MealController {

    private final MealApplicationService mealApplicationService;
    private final CurrentUserService currentUserService;

    public MealController(MealApplicationService mealApplicationService, CurrentUserService currentUserService) {
        this.mealApplicationService = mealApplicationService;
        this.currentUserService = currentUserService;
    }

    @PostMapping
    public ApiResponse<Map<String, Object>> createMeal(@Valid @RequestBody CreateMealRequest request) {
        List<MealApplicationService.ItemCommand> items = request.items() == null ? List.of() : request.items().stream()
                .map(item -> new MealApplicationService.ItemCommand(item.foodName(), item.amount(), item.unit(), item.calories(), item.protein(), item.fat(), item.carbs(), item.source(), item.confirmed()))
                .toList();
        MealApplicationService.CreateMealCommand command = new MealApplicationService.CreateMealCommand(
                request.mealType(), request.eatenAt(), request.description(), request.imageUrl(), request.recognitionTaskId(),
                request.selfRating(), request.emotionText(), items
        );
        return ApiResponse.success("餐次创建成功", mealApplicationService.createMeal(currentUserService.currentUserId(), command), TraceIdHolder.getTraceId());
    }

    @GetMapping
    public ApiResponse<List<Map<String, Object>>> listMeals() {
        return ApiResponse.success(mealApplicationService.listMeals(currentUserService.currentUserId()), TraceIdHolder.getTraceId());
    }

    public record CreateMealRequest(
            @NotBlank String mealType,
            @NotNull LocalDateTime eatenAt,
            String description,
            String imageUrl,
            Long recognitionTaskId,
            @NotNull @Min(1) @Max(5) Integer selfRating,
            String emotionText,
            List<ItemRequest> items
    ) {}

    public record ItemRequest(
            @NotBlank String foodName,
            @NotNull BigDecimal amount,
            @NotBlank String unit,
            @NotNull BigDecimal calories,
            @NotNull BigDecimal protein,
            @NotNull BigDecimal fat,
            @NotNull BigDecimal carbs,
            @NotBlank String source,
            boolean confirmed
    ) {}
}
