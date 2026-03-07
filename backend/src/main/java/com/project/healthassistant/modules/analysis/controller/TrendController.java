package com.project.healthassistant.modules.analysis.controller;

import com.project.healthassistant.common.api.ApiResponse;
import com.project.healthassistant.common.security.CurrentUserService;
import com.project.healthassistant.common.trace.TraceIdHolder;
import com.project.healthassistant.modules.analysis.application.TrendQueryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/analysis")
public class TrendController {

    private final TrendQueryService trendQueryService;
    private final CurrentUserService currentUserService;

    public TrendController(TrendQueryService trendQueryService, CurrentUserService currentUserService) {
        this.trendQueryService = trendQueryService;
        this.currentUserService = currentUserService;
    }

    @GetMapping("/trends")
    public ApiResponse<Map<String, Object>> trends() {
        return ApiResponse.success(trendQueryService.getTrends(currentUserService.currentUserId()), TraceIdHolder.getTraceId());
    }
}
