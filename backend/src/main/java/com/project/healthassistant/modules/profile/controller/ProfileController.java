package com.project.healthassistant.modules.profile.controller;

import com.project.healthassistant.common.api.ApiResponse;
import com.project.healthassistant.common.security.CurrentUserService;
import com.project.healthassistant.common.trace.TraceIdHolder;
import com.project.healthassistant.modules.profile.application.ProfileApplicationService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Map;

@Validated
@RestController
@RequestMapping("/api/v1/profile")
public class ProfileController {

    private final ProfileApplicationService profileApplicationService;
    private final CurrentUserService currentUserService;

    public ProfileController(ProfileApplicationService profileApplicationService, CurrentUserService currentUserService) {
        this.profileApplicationService = profileApplicationService;
        this.currentUserService = currentUserService;
    }

    @GetMapping
    public ApiResponse<Map<String, Object>> getProfile() {
        return ApiResponse.success(profileApplicationService.get(currentUserService.currentUserId()), TraceIdHolder.getTraceId());
    }

    @PutMapping
    public ApiResponse<Map<String, Object>> saveProfile(@RequestBody @Validated ProfileRequest request) {
        return ApiResponse.success("档案保存成功",
                profileApplicationService.save(currentUserService.currentUserId(), new ProfileApplicationService.ProfileCommand(
                        request.age(), request.gender(), request.heightCm(), request.weightKg(), request.activityLevel(),
                        request.goal(), request.allergies(), request.tabooFoods(), request.medicalConditions()
                )),
                TraceIdHolder.getTraceId());
    }

    public record ProfileRequest(
            @NotNull @Min(10) @Max(100) Integer age,
            @NotBlank String gender,
            @NotNull @Min(80) BigDecimal heightCm,
            @NotNull @Min(20) BigDecimal weightKg,
            @NotBlank String activityLevel,
            @NotBlank String goal,
            String allergies,
            String tabooFoods,
            String medicalConditions
    ) {
    }
}