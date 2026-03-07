package com.project.healthassistant.modules.meal.infrastructure;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.project.healthassistant.modules.meal.domain.MealItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface MealItemMapper extends BaseMapper<MealItem> {
    @Select("SELECT * FROM meal_item WHERE meal_record_id = #{mealRecordId} ORDER BY id ASC")
    List<MealItem> findByMealRecordId(Long mealRecordId);
}
