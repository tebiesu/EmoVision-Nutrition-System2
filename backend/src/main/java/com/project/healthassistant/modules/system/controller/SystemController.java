package com.project.healthassistant.modules.system.controller;

import com.project.healthassistant.common.api.ApiResponse;
import com.project.healthassistant.common.trace.TraceIdHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/system")
public class SystemController {

    @GetMapping("/ping")
    public ApiResponse<Map<String, Object>> ping() {
        return ApiResponse.success(Map.of(
                "status", "UP",
                "service", "health-assistant-backend",
                "timestamp", LocalDateTime.now().toString()
        ), TraceIdHolder.getTraceId());
    }
}
