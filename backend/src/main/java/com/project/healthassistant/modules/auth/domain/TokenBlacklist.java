package com.project.healthassistant.modules.auth.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("token_blacklist")
public class TokenBlacklist {
    @TableId(type = IdType.AUTO)
    private Long id;
    @TableField("token_id")
    private String tokenId;
    @TableField("user_id")
    private Long userId;
    @TableField("expired_at")
    private LocalDateTime expiredAt;
    @TableField("created_at")
    private LocalDateTime createdAt;
}
