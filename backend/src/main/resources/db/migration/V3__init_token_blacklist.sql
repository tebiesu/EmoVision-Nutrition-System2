CREATE TABLE IF NOT EXISTS token_blacklist (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    token_id VARCHAR(128) NOT NULL,
    user_id BIGINT NULL,
    expired_at DATETIME NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_token_blacklist_token_id (token_id),
    INDEX idx_token_blacklist_expired_at (expired_at)
);
