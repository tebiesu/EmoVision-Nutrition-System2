package com.project.healthassistant.modules.meal.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("nutrition_assessment")
public class NutritionAssessment {
    @TableId(type = IdType.AUTO)
    private Long id;
    @TableField("meal_record_id")
    private Long mealRecordId;
    @TableField("total_calories")
    private BigDecimal totalCalories;
    @TableField("total_protein")
    private BigDecimal totalProtein;
    @TableField("total_fat")
    private BigDecimal totalFat;
    @TableField("total_carbs")
    private BigDecimal totalCarbs;
    @TableField("structure_score")
    private BigDecimal structureScore;
    @TableField("risk_level")
    private String riskLevel;
    @TableField("risk_tags")
    private String riskTags;
    @TableField("evidence_text")
    private String evidenceText;
    @TableField("created_at")
    private LocalDateTime createdAt;
}
