package com.project.healthassistant.modules.system.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("api_audit_log")
public class ApiAuditLog {
    @TableId(type = IdType.AUTO)
    private Long id;
    @TableField("user_id")
    private Long userId;
    @TableField("request_uri")
    private String requestUri;
    @TableField("request_method")
    private String requestMethod;
    @TableField("request_body")
    private String requestBody;
    @TableField("response_code")
    private Integer responseCode;
    @TableField("duration_ms")
    private Long durationMs;
    private String ip;
    @TableField("trace_id")
    private String traceId;
    @TableField("created_at")
    private LocalDateTime createdAt;
}
