package com.project.healthassistant.modules.auth.application;

import com.project.healthassistant.common.api.ResultCode;
import com.project.healthassistant.common.exception.BusinessException;
import com.project.healthassistant.config.JwtProperties;
import com.project.healthassistant.modules.auth.domain.UserAccount;
import com.project.healthassistant.modules.auth.dto.LoginRequest;
import com.project.healthassistant.modules.auth.dto.LoginResponse;
import com.project.healthassistant.modules.auth.dto.RegisterRequest;
import com.project.healthassistant.modules.auth.infrastructure.UserAccountMapper;
import com.project.healthassistant.security.jwt.JwtTokenProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AuthApplicationService {

    private final UserAccountMapper userAccountMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtProperties jwtProperties;

    public AuthApplicationService(UserAccountMapper userAccountMapper,
                                  PasswordEncoder passwordEncoder,
                                  JwtTokenProvider jwtTokenProvider,
                                  JwtProperties jwtProperties) {
        this.userAccountMapper = userAccountMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.jwtProperties = jwtProperties;
    }

    @Transactional
    public Long register(RegisterRequest request) {
        UserAccount existing = userAccountMapper.findByUsername(request.getUsername());
        if (existing != null) {
            throw new BusinessException(ResultCode.USERNAME_EXISTS);
        }

        UserAccount user = new UserAccount();
        user.setUsername(request.getUsername());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setNickname(request.getNickname());
        user.setPhone(request.getPhone());
        user.setEmail(request.getEmail());
        user.setRoleCode("USER");
        user.setStatus(1);
        user.setDeleted(0);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        userAccountMapper.insert(user);
        return user.getId();
    }

    public LoginResponse login(LoginRequest request) {
        UserAccount user = userAccountMapper.findByUsername(request.getUsername());
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BusinessException(ResultCode.USERNAME_OR_PASSWORD_ERROR);
        }

        String token = jwtTokenProvider.generateToken(user.getId(), user.getUsername(), user.getRoleCode());
        return LoginResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .expiresIn(jwtProperties.getExpirationSeconds())
                .userInfo(LoginResponse.UserInfo.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .nickname(user.getNickname())
                        .roleCode(user.getRoleCode())
                        .build())
                .build();
    }
}
