package com.project.healthassistant.modules.vision.controller;

import com.project.healthassistant.common.api.ApiResponse;
import com.project.healthassistant.common.security.CurrentUserService;
import com.project.healthassistant.common.trace.TraceIdHolder;
import com.project.healthassistant.modules.vision.service.VisionRecognitionService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Validated
@RestController
@RequestMapping("/api/v1/vision")
public class VisionController {

    private final VisionRecognitionService visionRecognitionService;
    private final CurrentUserService currentUserService;

    public VisionController(VisionRecognitionService visionRecognitionService, CurrentUserService currentUserService) {
        this.visionRecognitionService = visionRecognitionService;
        this.currentUserService = currentUserService;
    }

    @PostMapping("/tasks")
    public ApiResponse<Map<String, Object>> createTask(@Valid @RequestBody VisionTaskRequest request) {
        return ApiResponse.success("vision task created",
                visionRecognitionService.createTask(currentUserService.currentUserId(), request.imageUrl(), request.description()),
                TraceIdHolder.getTraceId());
    }

    @GetMapping("/tasks/{taskId}")
    public ApiResponse<Map<String, Object>> getTask(@PathVariable Long taskId) {
        return ApiResponse.success(visionRecognitionService.getTask(taskId), TraceIdHolder.getTraceId());
    }

    public record VisionTaskRequest(String imageUrl, @NotBlank String description) {
    }
}
