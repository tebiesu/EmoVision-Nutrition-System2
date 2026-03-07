package com.project.healthassistant.modules.vision.infrastructure;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.project.healthassistant.modules.vision.domain.VisionRecognitionTask;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface VisionRecognitionTaskMapper extends BaseMapper<VisionRecognitionTask> {
}
