package com.project.healthassistant.modules.assistant.controller;

import com.project.healthassistant.common.api.ApiResponse;
import com.project.healthassistant.common.security.CurrentUserService;
import com.project.healthassistant.common.trace.TraceIdHolder;
import com.project.healthassistant.modules.assistant.service.NutritionAssistantService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Validated
@RestController
@RequestMapping("/api/v1/assistant")
public class NutritionAssistantController {

    private final NutritionAssistantService nutritionAssistantService;
    private final CurrentUserService currentUserService;

    public NutritionAssistantController(NutritionAssistantService nutritionAssistantService, CurrentUserService currentUserService) {
        this.nutritionAssistantService = nutritionAssistantService;
        this.currentUserService = currentUserService;
    }

    @PostMapping("/nutrition-chat")
    public ApiResponse<Map<String, Object>> chat(@Valid @RequestBody ChatRequest request) {
        NutritionAssistantService.ChatContextOptions options = new NutritionAssistantService.ChatContextOptions(
                request.includeProfile(),
                request.includeRecentMeals(),
                request.includeLatestAssessment()
        );
        return ApiResponse.success(
                nutritionAssistantService.chat(currentUserService.currentUserId(), request.message(), options),
                TraceIdHolder.getTraceId()
        );
    }

    public record ChatRequest(
            @NotBlank String message,
            boolean includeProfile,
            boolean includeRecentMeals,
            boolean includeLatestAssessment
    ) {
    }
}