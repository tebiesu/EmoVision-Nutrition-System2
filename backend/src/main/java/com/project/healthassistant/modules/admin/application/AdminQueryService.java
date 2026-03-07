package com.project.healthassistant.modules.admin.application;

import com.project.healthassistant.modules.auth.infrastructure.UserAccountMapper;
import com.project.healthassistant.modules.meal.infrastructure.MealRecordMapper;
import com.project.healthassistant.modules.system.service.ApiAuditLogService;
import com.project.healthassistant.modules.vision.infrastructure.VisionRecognitionTaskMapper;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class AdminQueryService {

    private final UserAccountMapper userAccountMapper;
    private final MealRecordMapper mealRecordMapper;
    private final VisionRecognitionTaskMapper visionRecognitionTaskMapper;
    private final ApiAuditLogService apiAuditLogService;

    public AdminQueryService(UserAccountMapper userAccountMapper,
                             MealRecordMapper mealRecordMapper,
                             VisionRecognitionTaskMapper visionRecognitionTaskMapper,
                             ApiAuditLogService apiAuditLogService) {
        this.userAccountMapper = userAccountMapper;
        this.mealRecordMapper = mealRecordMapper;
        this.visionRecognitionTaskMapper = visionRecognitionTaskMapper;
        this.apiAuditLogService = apiAuditLogService;
    }

    public Map<String, Object> overview() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("userCount", userAccountMapper.selectCount(null));
        result.put("mealCount", mealRecordMapper.selectCount(null));
        result.put("visionTaskCount", visionRecognitionTaskMapper.selectCount(null));
        result.put("recentAuditLogs", logs());
        return result;
    }

    public List<Map<String, Object>> logs() {
        return apiAuditLogService.latest(20).stream().map(log -> {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("traceId", log.getTraceId());
            row.put("requestUri", log.getRequestUri());
            row.put("requestMethod", log.getRequestMethod());
            row.put("responseCode", log.getResponseCode());
            row.put("durationMs", log.getDurationMs());
            row.put("createdAt", log.getCreatedAt());
            return row;
        }).toList();
    }
}
