package com.project.healthassistant.modules.vision.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("vision_recognition_task")
public class VisionRecognitionTask {
    @TableId(type = IdType.AUTO)
    private Long id;
    @TableField("user_id")
    private Long userId;
    @TableField("image_url")
    private String imageUrl;
    @TableField("description_text")
    private String descriptionText;
    private String status;
    @TableField("error_message")
    private String errorMessage;
    @TableField("created_at")
    private LocalDateTime createdAt;
    @TableField("finished_at")
    private LocalDateTime finishedAt;
}
