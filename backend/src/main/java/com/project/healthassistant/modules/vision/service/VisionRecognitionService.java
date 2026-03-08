package com.project.healthassistant.modules.vision.service;

import com.project.healthassistant.common.api.ResultCode;
import com.project.healthassistant.common.exception.BusinessException;
import com.project.healthassistant.modules.vision.domain.VisionRecognitionCandidate;
import com.project.healthassistant.modules.vision.domain.VisionRecognitionTask;
import com.project.healthassistant.modules.vision.infrastructure.VisionRecognitionCandidateMapper;
import com.project.healthassistant.modules.vision.infrastructure.VisionRecognitionTaskMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class VisionRecognitionService {

    private final VisionRecognitionTaskMapper taskMapper;
    private final VisionRecognitionCandidateMapper candidateMapper;
    private final VisionRecognitionWorker visionRecognitionWorker;

    public VisionRecognitionService(VisionRecognitionTaskMapper taskMapper,
                                    VisionRecognitionCandidateMapper candidateMapper,
                                    VisionRecognitionWorker visionRecognitionWorker) {
        this.taskMapper = taskMapper;
        this.candidateMapper = candidateMapper;
        this.visionRecognitionWorker = visionRecognitionWorker;
    }

    @Transactional
    public Map<String, Object> createTask(Long userId, String imageUrl, String description) {
        if (!StringUtils.hasText(imageUrl) && !StringUtils.hasText(description)) {
            throw new BusinessException(ResultCode.INVALID_PARAM.getCode(), "请至少上传图片或填写饮食描述");
        }

        VisionRecognitionTask task = new VisionRecognitionTask();
        task.setUserId(userId);
        task.setImageUrl(StringUtils.hasText(imageUrl) ? imageUrl.trim() : null);
        task.setDescriptionText(StringUtils.hasText(description) ? description.trim() : null);
        task.setStatus("PENDING");
        task.setCreatedAt(LocalDateTime.now());
        taskMapper.insert(task);

        visionRecognitionWorker.process(task.getId(), task.getImageUrl(), task.getDescriptionText());
        return toTaskResult(task, List.of());
    }

    public Map<String, Object> getTask(Long userId, Long taskId) {
        VisionRecognitionTask task = taskMapper.selectById(taskId);
        if (task == null || !userId.equals(task.getUserId())) {
            throw new BusinessException(ResultCode.RECORD_NOT_FOUND.getCode(), "识别任务不存在");
        }
        List<Map<String, Object>> candidates = candidateMapper.findByTaskId(taskId).stream()
                .map(this::toCandidateMap)
                .toList();
        return toTaskResult(task, candidates);
    }

    private Map<String, Object> toTaskResult(VisionRecognitionTask task, List<Map<String, Object>> candidates) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("taskId", task.getId());
        result.put("status", task.getStatus());
        result.put("errorMessage", task.getErrorMessage());
        result.put("createdAt", task.getCreatedAt());
        result.put("finishedAt", task.getFinishedAt());
        result.put("candidates", candidates);
        return result;
    }

    private Map<String, Object> toCandidateMap(VisionRecognitionCandidate candidate) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", candidate.getId());
        map.put("foodName", candidate.getFoodName());
        map.put("confidence", candidate.getConfidence());
        map.put("amount", candidate.getAmountSuggestion());
        map.put("unit", candidate.getUnit());
        map.put("calories", candidate.getCalories());
        map.put("protein", candidate.getProtein());
        map.put("fat", candidate.getFat());
        map.put("carbs", candidate.getCarbs());
        return map;
    }
}
