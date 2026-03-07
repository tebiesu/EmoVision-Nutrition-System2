package com.project.healthassistant.modules.auth.infrastructure;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.project.healthassistant.modules.auth.domain.TokenBlacklist;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface TokenBlacklistMapper extends BaseMapper<TokenBlacklist> {

    @Select("SELECT COUNT(1) FROM token_blacklist WHERE token_id = #{tokenId} AND expired_at > NOW()")
    long countActiveByTokenId(String tokenId);
}
