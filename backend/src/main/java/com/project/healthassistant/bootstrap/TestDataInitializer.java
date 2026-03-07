package com.project.healthassistant.bootstrap;

import com.project.healthassistant.modules.auth.domain.UserAccount;
import com.project.healthassistant.modules.auth.infrastructure.UserAccountMapper;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

@Configuration
@Profile("test")
public class TestDataInitializer {

    @Bean
    CommandLineRunner initTestUsers(UserAccountMapper userAccountMapper, PasswordEncoder passwordEncoder) {
        return args -> {
            ensureUser(userAccountMapper, passwordEncoder, "admin", "admin123", "ADMIN", "管理员");
            ensureUser(userAccountMapper, passwordEncoder, "demo", "123456", "USER", "演示用户");
        };
    }

    private void ensureUser(UserAccountMapper mapper, PasswordEncoder encoder, String username, String password, String roleCode, String nickname) {
        if (mapper.findByUsername(username) != null) {
            return;
        }
        UserAccount user = new UserAccount();
        user.setUsername(username);
        user.setPasswordHash(encoder.encode(password));
        user.setNickname(nickname);
        user.setRoleCode(roleCode);
        user.setStatus(1);
        user.setDeleted(0);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        mapper.insert(user);
    }
}
