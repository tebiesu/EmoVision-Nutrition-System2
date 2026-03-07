package com.project.healthassistant.modules.profile.application;

import com.project.healthassistant.common.api.ResultCode;
import com.project.healthassistant.common.exception.BusinessException;
import com.project.healthassistant.modules.profile.domain.UserProfile;
import com.project.healthassistant.modules.profile.infrastructure.UserProfileMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProfileApplicationServiceTest {

    @Mock
    private UserProfileMapper userProfileMapper;

    @InjectMocks
    private ProfileApplicationService profileApplicationService;

    @Test
    void shouldCalculateBmiAndBmr() {
        BigDecimal bmi = profileApplicationService.calculateBmi(BigDecimal.valueOf(175), BigDecimal.valueOf(70));
        BigDecimal bmr = profileApplicationService.calculateBmr("male", BigDecimal.valueOf(175), BigDecimal.valueOf(70), 25);

        assertEquals(new BigDecimal("22.86"), bmi);
        assertTrue(bmr.compareTo(BigDecimal.valueOf(1600)) > 0);
    }

    @Test
    void shouldThrowWhenProfileMissing() {
        when(userProfileMapper.findByUserId(1L)).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class, () -> profileApplicationService.get(1L));

        assertEquals(ResultCode.PROFILE_NOT_FOUND.getCode(), exception.getCode());
    }
}
