package com.project.healthassistant.modules.profile.infrastructure;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.project.healthassistant.modules.profile.domain.UserProfile;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserProfileMapper extends BaseMapper<UserProfile> {

    @Select("SELECT * FROM user_profile WHERE user_id = #{userId} LIMIT 1")
    UserProfile findByUserId(Long userId);
}
