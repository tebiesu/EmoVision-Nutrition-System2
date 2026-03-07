DROP TABLE IF EXISTS recommendation_record;
DROP TABLE IF EXISTS nutrition_assessment;
DROP TABLE IF EXISTS emotion_record;
DROP TABLE IF EXISTS meal_item;
DROP TABLE IF EXISTS meal_record;
DROP TABLE IF EXISTS vision_recognition_candidate;
DROP TABLE IF EXISTS vision_recognition_task;
DROP TABLE IF EXISTS upload_file;
DROP TABLE IF EXISTS daily_health_summary;
DROP TABLE IF EXISTS user_profile;
DROP TABLE IF EXISTS api_audit_log;
DROP TABLE IF EXISTS token_blacklist;
DROP TABLE IF EXISTS user_account;

CREATE TABLE user_account (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(64) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    nickname VARCHAR(64),
    phone VARCHAR(32),
    email VARCHAR(128),
    avatar_url VARCHAR(255),
    role_code VARCHAR(32) NOT NULL DEFAULT 'USER',
    status TINYINT NOT NULL DEFAULT 1,
    deleted TINYINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_user_account_username UNIQUE (username)
);

CREATE TABLE token_blacklist (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    token_id VARCHAR(128) NOT NULL,
    user_id BIGINT,
    expired_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE api_audit_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT,
    request_uri VARCHAR(255) NOT NULL,
    request_method VARCHAR(16) NOT NULL,
    request_body CLOB,
    response_code INT,
    duration_ms BIGINT,
    ip VARCHAR(64),
    trace_id VARCHAR(64) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE user_profile (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    age INT,
    gender VARCHAR(16),
    height_cm DECIMAL(10,2),
    weight_kg DECIMAL(10,2),
    activity_level VARCHAR(32),
    goal VARCHAR(32),
    allergies CLOB,
    taboo_foods CLOB,
    medical_conditions CLOB,
    bmi DECIMAL(10,2),
    bmr DECIMAL(10,2),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_user_profile_user_id UNIQUE (user_id)
);

CREATE TABLE upload_file (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    original_name VARCHAR(255) NOT NULL,
    storage_path VARCHAR(255) NOT NULL,
    content_type VARCHAR(128) NOT NULL,
    size_bytes BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE vision_recognition_task (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    image_url VARCHAR(255),
    description_text VARCHAR(255),
    status VARCHAR(32) NOT NULL,
    error_message VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    finished_at TIMESTAMP
);

CREATE TABLE vision_recognition_candidate (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    task_id BIGINT NOT NULL,
    food_name VARCHAR(128) NOT NULL,
    confidence DECIMAL(10,4) NOT NULL,
    amount_suggestion DECIMAL(10,2) NOT NULL,
    unit VARCHAR(32) NOT NULL,
    calories DECIMAL(10,2) NOT NULL,
    protein DECIMAL(10,2) NOT NULL,
    fat DECIMAL(10,2) NOT NULL,
    carbs DECIMAL(10,2) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE meal_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    meal_type VARCHAR(32) NOT NULL,
    eaten_at TIMESTAMP NOT NULL,
    description_text VARCHAR(255),
    image_url VARCHAR(255),
    recognition_task_id BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE meal_item (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    meal_record_id BIGINT NOT NULL,
    food_name VARCHAR(128) NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    unit VARCHAR(32) NOT NULL,
    calories DECIMAL(10,2) NOT NULL,
    protein DECIMAL(10,2) NOT NULL,
    fat DECIMAL(10,2) NOT NULL,
    carbs DECIMAL(10,2) NOT NULL,
    source VARCHAR(32) NOT NULL,
    confirmed TINYINT NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE emotion_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    meal_record_id BIGINT NOT NULL,
    self_rating INT NOT NULL,
    text_content VARCHAR(255),
    sentiment_label VARCHAR(32) NOT NULL,
    sentiment_score DECIMAL(10,2) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE nutrition_assessment (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    meal_record_id BIGINT NOT NULL,
    total_calories DECIMAL(10,2) NOT NULL,
    total_protein DECIMAL(10,2) NOT NULL,
    total_fat DECIMAL(10,2) NOT NULL,
    total_carbs DECIMAL(10,2) NOT NULL,
    structure_score DECIMAL(10,2) NOT NULL,
    risk_level VARCHAR(16) NOT NULL,
    risk_tags CLOB,
    evidence_text CLOB,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE recommendation_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    meal_record_id BIGINT NOT NULL,
    ai_enhanced TINYINT NOT NULL DEFAULT 0,
    fallback_mode TINYINT NOT NULL DEFAULT 0,
    summary_text CLOB NOT NULL,
    recommendation_text CLOB NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE daily_health_summary (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    summary_date DATE NOT NULL,
    total_calories DECIMAL(10,2) NOT NULL,
    avg_emotion_score DECIMAL(10,2) NOT NULL,
    summary_text CLOB NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_daily_health_summary UNIQUE (user_id, summary_date)
);
