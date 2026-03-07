package com.project.healthassistant.modules.meal.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("meal_item")
public class MealItem {
    @TableId(type = IdType.AUTO)
    private Long id;
    @TableField("meal_record_id")
    private Long mealRecordId;
    @TableField("food_name")
    private String foodName;
    private BigDecimal amount;
    private String unit;
    private BigDecimal calories;
    private BigDecimal protein;
    private BigDecimal fat;
    private BigDecimal carbs;
    private String source;
    private Integer confirmed;
    @TableField("created_at")
    private LocalDateTime createdAt;
}
