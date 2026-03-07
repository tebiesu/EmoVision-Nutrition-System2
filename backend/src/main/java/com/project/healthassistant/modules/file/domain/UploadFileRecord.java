package com.project.healthassistant.modules.file.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("upload_file")
public class UploadFileRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    @TableField("user_id")
    private Long userId;
    @TableField("original_name")
    private String originalName;
    @TableField("storage_path")
    private String storagePath;
    @TableField("content_type")
    private String contentType;
    @TableField("size_bytes")
    private Long sizeBytes;
    @TableField("created_at")
    private LocalDateTime createdAt;
}
