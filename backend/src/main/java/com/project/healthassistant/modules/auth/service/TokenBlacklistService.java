package com.project.healthassistant.modules.auth.service;

import com.project.healthassistant.modules.auth.domain.TokenBlacklist;
import com.project.healthassistant.modules.auth.infrastructure.TokenBlacklistMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class TokenBlacklistService {

    private final TokenBlacklistMapper tokenBlacklistMapper;

    public TokenBlacklistService(TokenBlacklistMapper tokenBlacklistMapper) {
        this.tokenBlacklistMapper = tokenBlacklistMapper;
    }

    public void blacklist(String tokenId, Long userId, LocalDateTime expiredAt) {
        TokenBlacklist entity = new TokenBlacklist();
        entity.setTokenId(tokenId);
        entity.setUserId(userId);
        entity.setExpiredAt(expiredAt);
        entity.setCreatedAt(LocalDateTime.now());
        tokenBlacklistMapper.insert(entity);
    }

    public boolean isBlacklisted(String tokenId) {
        return tokenBlacklistMapper.countActiveByTokenId(tokenId) > 0;
    }
}
