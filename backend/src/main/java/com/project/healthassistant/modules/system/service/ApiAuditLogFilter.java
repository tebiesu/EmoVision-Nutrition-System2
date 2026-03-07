package com.project.healthassistant.modules.system.service;

import com.project.healthassistant.common.trace.TraceIdHolder;
import com.project.healthassistant.modules.system.domain.ApiAuditLog;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;

@Component
public class ApiAuditLogFilter extends OncePerRequestFilter {

    private final ApiAuditLogService apiAuditLogService;

    public ApiAuditLogFilter(ApiAuditLogService apiAuditLogService) {
        this.apiAuditLogService = apiAuditLogService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        Instant start = Instant.now();
        try {
            filterChain.doFilter(request, response);
        } finally {
            if (!request.getRequestURI().startsWith("/actuator")) {
                ApiAuditLog log = new ApiAuditLog();
                log.setRequestUri(request.getRequestURI());
                log.setRequestMethod(request.getMethod());
                log.setResponseCode(response.getStatus());
                log.setDurationMs(Duration.between(start, Instant.now()).toMillis());
                log.setIp(request.getRemoteAddr());
                log.setTraceId(TraceIdHolder.getTraceId());
                apiAuditLogService.save(log);
            }
        }
    }
}
