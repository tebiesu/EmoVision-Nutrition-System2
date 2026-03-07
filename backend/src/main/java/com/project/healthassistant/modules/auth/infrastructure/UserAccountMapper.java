package com.project.healthassistant.modules.auth.infrastructure;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.project.healthassistant.modules.auth.domain.UserAccount;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserAccountMapper extends BaseMapper<UserAccount> {

    @Select("SELECT * FROM user_account WHERE username = #{username} AND deleted = 0 LIMIT 1")
    UserAccount findByUsername(String username);
}
