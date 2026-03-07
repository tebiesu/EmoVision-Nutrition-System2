package com.project.healthassistant.common.security;

public record AppUserPrincipal(Long userId, String username, String roleCode) {
}
