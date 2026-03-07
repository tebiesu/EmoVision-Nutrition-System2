package com.project.healthassistant.modules.profile.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("user_profile")
public class UserProfile {
    @TableId(type = IdType.AUTO)
    private Long id;
    @TableField("user_id")
    private Long userId;
    private Integer age;
    private String gender;
    @TableField("height_cm")
    private BigDecimal heightCm;
    @TableField("weight_kg")
    private BigDecimal weightKg;
    @TableField("activity_level")
    private String activityLevel;
    private String goal;
    private String allergies;
    @TableField("taboo_foods")
    private String tabooFoods;
    @TableField("medical_conditions")
    private String medicalConditions;
    private BigDecimal bmi;
    private BigDecimal bmr;
    @TableField("created_at")
    private LocalDateTime createdAt;
    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
