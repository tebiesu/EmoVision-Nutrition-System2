CREATE TABLE IF NOT EXISTS api_audit_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NULL,
    request_uri VARCHAR(255) NOT NULL,
    request_method VARCHAR(16) NOT NULL,
    request_body TEXT NULL,
    response_code INT NULL,
    duration_ms BIGINT NULL,
    ip VARCHAR(64) NULL,
    trace_id VARCHAR(64) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_api_audit_log_trace_id (trace_id),
    INDEX idx_api_audit_log_created_at (created_at)
);
