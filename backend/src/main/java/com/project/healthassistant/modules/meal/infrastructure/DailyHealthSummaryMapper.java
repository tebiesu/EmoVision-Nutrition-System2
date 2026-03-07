package com.project.healthassistant.modules.meal.infrastructure;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.project.healthassistant.modules.meal.domain.DailyHealthSummary;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface DailyHealthSummaryMapper extends BaseMapper<DailyHealthSummary> {
    @Select("SELECT * FROM daily_health_summary WHERE user_id = #{userId} AND summary_date = #{summaryDate} LIMIT 1")
    DailyHealthSummary findByUserIdAndDate(Long userId, java.time.LocalDate summaryDate);
}
