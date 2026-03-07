package com.project.healthassistant.modules.auth.domain;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserAccount {
    private Long id;
    private String username;
    private String passwordHash;
    private String nickname;
    private String phone;
    private String email;
    private String avatarUrl;
    private String roleCode;
    private Integer status;
    private Integer deleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
