package com.project.healthassistant.modules.meal.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("emotion_record")
public class EmotionRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    @TableField("meal_record_id")
    private Long mealRecordId;
    @TableField("self_rating")
    private Integer selfRating;
    @TableField("text_content")
    private String textContent;
    @TableField("sentiment_label")
    private String sentimentLabel;
    @TableField("sentiment_score")
    private BigDecimal sentimentScore;
    @TableField("created_at")
    private LocalDateTime createdAt;
}
