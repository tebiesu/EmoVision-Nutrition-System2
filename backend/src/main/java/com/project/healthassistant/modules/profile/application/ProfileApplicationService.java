package com.project.healthassistant.modules.profile.application;

import com.project.healthassistant.common.api.ResultCode;
import com.project.healthassistant.common.exception.BusinessException;
import com.project.healthassistant.modules.profile.domain.UserProfile;
import com.project.healthassistant.modules.profile.infrastructure.UserProfileMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class ProfileApplicationService {

    private final UserProfileMapper userProfileMapper;

    public ProfileApplicationService(UserProfileMapper userProfileMapper) {
        this.userProfileMapper = userProfileMapper;
    }

    @Transactional
    public Map<String, Object> save(Long userId, ProfileCommand command) {
        UserProfile profile = userProfileMapper.findByUserId(userId);
        if (profile == null) {
            profile = new UserProfile();
            profile.setUserId(userId);
            profile.setCreatedAt(LocalDateTime.now());
        }

        profile.setAge(command.age());
        profile.setGender(command.gender());
        profile.setHeightCm(command.heightCm());
        profile.setWeightKg(command.weightKg());
        profile.setActivityLevel(command.activityLevel());
        profile.setGoal(command.goal());
        profile.setAllergies(command.allergies());
        profile.setTabooFoods(command.tabooFoods());
        profile.setMedicalConditions(command.medicalConditions());
        profile.setBmi(calculateBmi(command.heightCm(), command.weightKg()));
        profile.setBmr(calculateBmr(command.gender(), command.heightCm(), command.weightKg(), command.age()));
        profile.setUpdatedAt(LocalDateTime.now());

        if (profile.getId() == null) {
            userProfileMapper.insert(profile);
        } else {
            userProfileMapper.updateById(profile);
        }
        return toMap(profile);
    }

    public Map<String, Object> get(Long userId) {
        UserProfile profile = userProfileMapper.findByUserId(userId);
        if (profile == null) {
            throw new BusinessException(ResultCode.PROFILE_NOT_FOUND);
        }
        return toMap(profile);
    }

    public UserProfile requireProfile(Long userId) {
        UserProfile profile = userProfileMapper.findByUserId(userId);
        if (profile == null) {
            throw new BusinessException(ResultCode.PROFILE_NOT_FOUND);
        }
        return profile;
    }

    public BigDecimal calculateBmi(BigDecimal heightCm, BigDecimal weightKg) {
        BigDecimal meter = heightCm.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
        return weightKg.divide(meter.multiply(meter), 2, RoundingMode.HALF_UP);
    }

    public BigDecimal calculateBmr(String gender, BigDecimal heightCm, BigDecimal weightKg, Integer age) {
        BigDecimal result = weightKg.multiply(BigDecimal.valueOf(10))
                .add(heightCm.multiply(BigDecimal.valueOf(6.25)))
                .subtract(BigDecimal.valueOf(age).multiply(BigDecimal.valueOf(5)));
        result = result.add("female".equalsIgnoreCase(gender) ? BigDecimal.valueOf(-161) : BigDecimal.valueOf(5));
        return result.setScale(2, RoundingMode.HALF_UP);
    }

    private Map<String, Object> toMap(UserProfile profile) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("userId", profile.getUserId());
        data.put("age", profile.getAge());
        data.put("gender", profile.getGender());
        data.put("heightCm", profile.getHeightCm());
        data.put("weightKg", profile.getWeightKg());
        data.put("activityLevel", profile.getActivityLevel());
        data.put("goal", profile.getGoal());
        data.put("allergies", profile.getAllergies());
        data.put("tabooFoods", profile.getTabooFoods());
        data.put("medicalConditions", profile.getMedicalConditions());
        data.put("bmi", profile.getBmi());
        data.put("bmr", profile.getBmr());
        return data;
    }

    public record ProfileCommand(
            Integer age,
            String gender,
            BigDecimal heightCm,
            BigDecimal weightKg,
            String activityLevel,
            String goal,
            String allergies,
            String tabooFoods,
            String medicalConditions
    ) {}
}
