package com.project.healthassistant.modules.system.service;

import com.project.healthassistant.modules.system.domain.ApiAuditLog;
import com.project.healthassistant.modules.system.infrastructure.ApiAuditLogMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ApiAuditLogService {

    private final ApiAuditLogMapper apiAuditLogMapper;

    public ApiAuditLogService(ApiAuditLogMapper apiAuditLogMapper) {
        this.apiAuditLogMapper = apiAuditLogMapper;
    }

    public void save(ApiAuditLog log) {
        log.setCreatedAt(LocalDateTime.now());
        apiAuditLogMapper.insert(log);
    }

    public List<ApiAuditLog> latest(int limit) {
        return apiAuditLogMapper.selectList(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ApiAuditLog>()
                .orderByDesc(ApiAuditLog::getId)
                .last("LIMIT " + limit));
    }
}
