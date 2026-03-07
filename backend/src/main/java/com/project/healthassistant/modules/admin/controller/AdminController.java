package com.project.healthassistant.modules.admin.controller;

import com.project.healthassistant.common.api.ApiResponse;
import com.project.healthassistant.common.trace.TraceIdHolder;
import com.project.healthassistant.modules.admin.application.AdminQueryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {

    private final AdminQueryService adminQueryService;

    public AdminController(AdminQueryService adminQueryService) {
        this.adminQueryService = adminQueryService;
    }

    @GetMapping("/overview")
    public ApiResponse<Map<String, Object>> overview() {
        return ApiResponse.success(adminQueryService.overview(), TraceIdHolder.getTraceId());
    }

    @GetMapping("/audit-logs")
    public ApiResponse<List<Map<String, Object>>> auditLogs() {
        return ApiResponse.success(adminQueryService.logs(), TraceIdHolder.getTraceId());
    }
}
