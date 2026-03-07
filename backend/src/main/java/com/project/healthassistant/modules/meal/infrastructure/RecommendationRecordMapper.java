package com.project.healthassistant.modules.meal.infrastructure;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.project.healthassistant.modules.meal.domain.RecommendationRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface RecommendationRecordMapper extends BaseMapper<RecommendationRecord> {
    @Select("SELECT * FROM recommendation_record WHERE meal_record_id = #{mealRecordId} LIMIT 1")
    RecommendationRecord findByMealRecordId(Long mealRecordId);
}
