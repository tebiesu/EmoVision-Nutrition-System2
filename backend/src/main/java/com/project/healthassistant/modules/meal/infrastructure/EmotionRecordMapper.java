package com.project.healthassistant.modules.meal.infrastructure;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.project.healthassistant.modules.meal.domain.EmotionRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface EmotionRecordMapper extends BaseMapper<EmotionRecord> {
    @Select("SELECT * FROM emotion_record WHERE meal_record_id = #{mealRecordId} LIMIT 1")
    EmotionRecord findByMealRecordId(Long mealRecordId);
}
