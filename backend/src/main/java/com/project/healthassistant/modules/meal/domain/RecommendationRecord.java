package com.project.healthassistant.modules.meal.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("recommendation_record")
public class RecommendationRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    @TableField("meal_record_id")
    private Long mealRecordId;
    @TableField("ai_enhanced")
    private Integer aiEnhanced;
    @TableField("fallback_mode")
    private Integer fallbackMode;
    @TableField("summary_text")
    private String summaryText;
    @TableField("recommendation_text")
    private String recommendationText;
    @TableField("created_at")
    private LocalDateTime createdAt;
}
