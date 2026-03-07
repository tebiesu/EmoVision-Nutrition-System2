package com.project.healthassistant.modules.auth.application;

import com.project.healthassistant.common.api.ResultCode;
import com.project.healthassistant.common.exception.BusinessException;
import com.project.healthassistant.config.JwtProperties;
import com.project.healthassistant.modules.auth.domain.UserAccount;
import com.project.healthassistant.modules.auth.dto.LoginRequest;
import com.project.healthassistant.modules.auth.dto.LoginResponse;
import com.project.healthassistant.modules.auth.dto.RegisterRequest;
import com.project.healthassistant.modules.auth.infrastructure.UserAccountMapper;
import com.project.healthassistant.modules.auth.service.TokenBlacklistService;
import com.project.healthassistant.security.jwt.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthApplicationServiceTest {

    @Mock
    private UserAccountMapper userAccountMapper;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private TokenBlacklistService tokenBlacklistService;

    private JwtTokenProvider jwtTokenProvider;
    private AuthApplicationService authApplicationService;

    @BeforeEach
    void setUp() {
        JwtProperties properties = new JwtProperties();
        properties.setSecret("1234567890123456789012345678901234567890");
        properties.setExpirationSeconds(7200);
        jwtTokenProvider = new JwtTokenProvider(properties);
        authApplicationService = new AuthApplicationService(userAccountMapper, passwordEncoder, jwtTokenProvider, properties, tokenBlacklistService);
    }

    @Test
    void shouldRejectDuplicateUsername() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("demo");
        when(userAccountMapper.findByUsername("demo")).thenReturn(new UserAccount());

        BusinessException exception = assertThrows(BusinessException.class, () -> authApplicationService.register(request));

        assertEquals(ResultCode.USERNAME_EXISTS.getCode(), exception.getCode());
    }

    @Test
    void shouldLoginSuccessfully() {
        UserAccount user = new UserAccount();
        user.setId(1L);
        user.setUsername("demo");
        user.setNickname("Demo");
        user.setRoleCode("USER");
        user.setPasswordHash("encoded");
        when(userAccountMapper.findByUsername("demo")).thenReturn(user);
        when(passwordEncoder.matches("123456", "encoded")).thenReturn(true);
        LoginRequest request = new LoginRequest();
        request.setUsername("demo");
        request.setPassword("123456");

        LoginResponse response = authApplicationService.login(request);

        assertEquals("demo", response.getUserInfo().getUsername());
        assertNotNull(response.getAccessToken());
    }

    @Test
    void shouldBlacklistTokenOnLogout() {
        String token = jwtTokenProvider.generateToken(1L, "demo", "USER");

        authApplicationService.logout(1L, token);

        verify(tokenBlacklistService).blacklist(any(), eq(1L), any());
    }
}
