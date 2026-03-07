package com.project.healthassistant.modules.meal.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("meal_record")
public class MealRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    @TableField("user_id")
    private Long userId;
    @TableField("meal_type")
    private String mealType;
    @TableField("eaten_at")
    private LocalDateTime eatenAt;
    @TableField("description_text")
    private String descriptionText;
    @TableField("image_url")
    private String imageUrl;
    @TableField("recognition_task_id")
    private Long recognitionTaskId;
    @TableField("created_at")
    private LocalDateTime createdAt;
    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
