package com.project.healthassistant.modules.vision.infrastructure;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.project.healthassistant.modules.vision.domain.VisionRecognitionCandidate;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface VisionRecognitionCandidateMapper extends BaseMapper<VisionRecognitionCandidate> {

    @Select("SELECT * FROM vision_recognition_candidate WHERE task_id = #{taskId} ORDER BY id ASC")
    List<VisionRecognitionCandidate> findByTaskId(Long taskId);
}
