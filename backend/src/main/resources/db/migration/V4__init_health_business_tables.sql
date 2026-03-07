CREATE TABLE IF NOT EXISTS user_profile (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    age INT NULL,
    gender VARCHAR(16) NULL,
    height_cm DECIMAL(10,2) NULL,
    weight_kg DECIMAL(10,2) NULL,
    activity_level VARCHAR(32) NULL,
    goal VARCHAR(32) NULL,
    allergies TEXT NULL,
    taboo_foods TEXT NULL,
    medical_conditions TEXT NULL,
    bmi DECIMAL(10,2) NULL,
    bmr DECIMAL(10,2) NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uk_user_profile_user_id UNIQUE (user_id)
);

CREATE TABLE IF NOT EXISTS upload_file (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    original_name VARCHAR(255) NOT NULL,
    storage_path VARCHAR(255) NOT NULL,
    content_type VARCHAR(128) NOT NULL,
    size_bytes BIGINT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS vision_recognition_task (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    image_url VARCHAR(255) NULL,
    description_text VARCHAR(255) NULL,
    status VARCHAR(32) NOT NULL,
    error_message VARCHAR(255) NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    finished_at DATETIME NULL
);

CREATE TABLE IF NOT EXISTS vision_recognition_candidate (
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
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_vision_candidate_task_id (task_id)
);

CREATE TABLE IF NOT EXISTS meal_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    meal_type VARCHAR(32) NOT NULL,
    eaten_at DATETIME NOT NULL,
    description_text VARCHAR(255) NULL,
    image_url VARCHAR(255) NULL,
    recognition_task_id BIGINT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_meal_record_user_time (user_id, eaten_at)
);

CREATE TABLE IF NOT EXISTS meal_item (
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
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_meal_item_record_id (meal_record_id)
);

CREATE TABLE IF NOT EXISTS emotion_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    meal_record_id BIGINT NOT NULL,
    self_rating INT NOT NULL,
    text_content VARCHAR(255) NULL,
    sentiment_label VARCHAR(32) NOT NULL,
    sentiment_score DECIMAL(10,2) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_emotion_record_meal_record_id (meal_record_id)
);

CREATE TABLE IF NOT EXISTS nutrition_assessment (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    meal_record_id BIGINT NOT NULL,
    total_calories DECIMAL(10,2) NOT NULL,
    total_protein DECIMAL(10,2) NOT NULL,
    total_fat DECIMAL(10,2) NOT NULL,
    total_carbs DECIMAL(10,2) NOT NULL,
    structure_score DECIMAL(10,2) NOT NULL,
    risk_level VARCHAR(16) NOT NULL,
    risk_tags TEXT NULL,
    evidence_text TEXT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_nutrition_assessment_meal_record_id (meal_record_id)
);

CREATE TABLE IF NOT EXISTS recommendation_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    meal_record_id BIGINT NOT NULL,
    ai_enhanced TINYINT NOT NULL DEFAULT 0,
    fallback_mode TINYINT NOT NULL DEFAULT 0,
    summary_text TEXT NOT NULL,
    recommendation_text TEXT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_recommendation_record_meal_record_id (meal_record_id)
);

CREATE TABLE IF NOT EXISTS daily_health_summary (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    summary_date DATE NOT NULL,
    total_calories DECIMAL(10,2) NOT NULL,
    avg_emotion_score DECIMAL(10,2) NOT NULL,
    summary_text TEXT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uk_daily_health_summary UNIQUE (user_id, summary_date)
);
