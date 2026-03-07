package com.project.healthassistant.modules.meal.infrastructure;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.project.healthassistant.modules.meal.domain.NutritionAssessment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface NutritionAssessmentMapper extends BaseMapper<NutritionAssessment> {
    @Select("SELECT * FROM nutrition_assessment WHERE meal_record_id = #{mealRecordId} LIMIT 1")
    NutritionAssessment findByMealRecordId(Long mealRecordId);
}
