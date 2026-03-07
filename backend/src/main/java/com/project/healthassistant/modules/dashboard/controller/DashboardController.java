package com.project.healthassistant.modules.dashboard.controller;

import com.project.healthassistant.common.api.ApiResponse;
import com.project.healthassistant.common.security.CurrentUserService;
import com.project.healthassistant.common.trace.TraceIdHolder;
import com.project.healthassistant.modules.dashboard.application.DashboardQueryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/dashboard")
public class DashboardController {

    private final DashboardQueryService dashboardQueryService;
    private final CurrentUserService currentUserService;

    public DashboardController(DashboardQueryService dashboardQueryService, CurrentUserService currentUserService) {
        this.dashboardQueryService = dashboardQueryService;
        this.currentUserService = currentUserService;
    }

    @GetMapping("/summary")
    public ApiResponse<Map<String, Object>> summary() {
        return ApiResponse.success(dashboardQueryService.getSummary(currentUserService.currentUserId()), TraceIdHolder.getTraceId());
    }
}
