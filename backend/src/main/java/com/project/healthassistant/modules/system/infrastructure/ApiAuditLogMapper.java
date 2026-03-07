package com.project.healthassistant.modules.system.infrastructure;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.project.healthassistant.modules.system.domain.ApiAuditLog;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ApiAuditLogMapper extends BaseMapper<ApiAuditLog> {
}
