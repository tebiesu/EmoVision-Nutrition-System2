package com.project.healthassistant.modules.meal.infrastructure;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.project.healthassistant.modules.meal.domain.MealRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface MealRecordMapper extends BaseMapper<MealRecord> {
    @Select("SELECT * FROM meal_record WHERE user_id = #{userId} ORDER BY eaten_at DESC")
    List<MealRecord> findByUserId(Long userId);
}
