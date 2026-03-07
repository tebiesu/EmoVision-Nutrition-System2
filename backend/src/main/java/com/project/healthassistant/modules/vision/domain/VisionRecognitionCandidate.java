package com.project.healthassistant.modules.vision.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("vision_recognition_candidate")
public class VisionRecognitionCandidate {
    @TableId(type = IdType.AUTO)
    private Long id;
    @TableField("task_id")
    private Long taskId;
    @TableField("food_name")
    private String foodName;
    private BigDecimal confidence;
    @TableField("amount_suggestion")
    private BigDecimal amountSuggestion;
    private String unit;
    private BigDecimal calories;
    private BigDecimal protein;
    private BigDecimal fat;
    private BigDecimal carbs;
    @TableField("created_at")
    private LocalDateTime createdAt;
}
