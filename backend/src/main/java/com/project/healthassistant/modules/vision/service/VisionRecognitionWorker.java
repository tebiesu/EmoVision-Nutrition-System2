package com.project.healthassistant.modules.vision.service;

import com.project.healthassistant.modules.vision.domain.VisionRecognitionCandidate;
import com.project.healthassistant.modules.vision.domain.VisionRecognitionTask;
import com.project.healthassistant.modules.vision.infrastructure.VisionRecognitionCandidateMapper;
import com.project.healthassistant.modules.vision.infrastructure.VisionRecognitionTaskMapper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class VisionRecognitionWorker {

    private final VisionRecognitionTaskMapper taskMapper;
    private final VisionRecognitionCandidateMapper candidateMapper;
    private final VisionModelClient visionModelClient;

    public VisionRecognitionWorker(VisionRecognitionTaskMapper taskMapper,
                                   VisionRecognitionCandidateMapper candidateMapper,
                                   VisionModelClient visionModelClient) {
        this.taskMapper = taskMapper;
        this.candidateMapper = candidateMapper;
        this.visionModelClient = visionModelClient;
    }

    @Async
    public void process(Long taskId, String imageUrl, String description) {
        VisionRecognitionTask task = taskMapper.selectById(taskId);
        if (task == null) {
            return;
        }

        try {
            task.setStatus("PROCESSING");
            taskMapper.updateById(task);

            VisionModelClient.VisionRecognitionResult result = visionModelClient.recognize(imageUrl, description);
            for (VisionModelClient.RecognizedFood food : result.foods()) {
                VisionRecognitionCandidate candidate = new VisionRecognitionCandidate();
                candidate.setTaskId(taskId);
                candidate.setFoodName(food.foodName());
                candidate.setConfidence(food.confidence());
                candidate.setAmountSuggestion(food.amount());
                candidate.setUnit(food.unit());
                candidate.setCalories(food.calories());
                candidate.setProtein(food.protein());
                candidate.setFat(food.fat());
                candidate.setCarbs(food.carbs());
                candidate.setCreatedAt(LocalDateTime.now());
                candidateMapper.insert(candidate);
            }

            task.setStatus(result.foods().isEmpty() ? "EMPTY" : "SUCCESS");
            task.setErrorMessage(result.usedAi() ? null : result.message());
            task.setFinishedAt(LocalDateTime.now());
            taskMapper.updateById(task);
        } catch (Exception ex) {
            task.setStatus("FAILED");
            task.setErrorMessage(ex.getMessage());
            task.setFinishedAt(LocalDateTime.now());
            taskMapper.updateById(task);
        }
    }
}
