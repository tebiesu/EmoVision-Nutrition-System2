package com.project.healthassistant.modules.vision.service;

import com.project.healthassistant.common.exception.BusinessException;
import com.project.healthassistant.modules.vision.domain.VisionRecognitionCandidate;
import com.project.healthassistant.modules.vision.domain.VisionRecognitionTask;
import com.project.healthassistant.modules.vision.infrastructure.VisionRecognitionCandidateMapper;
import com.project.healthassistant.modules.vision.infrastructure.VisionRecognitionTaskMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VisionRecognitionServiceTest {

    @Mock
    private VisionRecognitionTaskMapper taskMapper;
    @Mock
    private VisionRecognitionCandidateMapper candidateMapper;
    @Mock
    private VisionRecognitionWorker visionRecognitionWorker;

    @InjectMocks
    private VisionRecognitionService visionRecognitionService;

    @Test
    void shouldCreatePendingTaskAndDispatchWorker() {
        doAnswer(invocation -> {
            VisionRecognitionTask task = invocation.getArgument(0);
            task.setId(1L);
            return 1;
        }).when(taskMapper).insert(any(VisionRecognitionTask.class));

        Map<String, Object> result = visionRecognitionService.createTask(1L, "/uploads/demo.png", "鸡胸肉饭");

        assertEquals("PENDING", result.get("status"));
        assertTrue(((List<?>) result.get("candidates")).isEmpty());
        verify(visionRecognitionWorker).process(1L, "/uploads/demo.png", "鸡胸肉饭");
    }

    @Test
    void shouldReturnCandidatesForTaskOwner() {
        VisionRecognitionTask task = new VisionRecognitionTask();
        task.setId(2L);
        task.setUserId(1L);
        task.setStatus("SUCCESS");
        task.setCreatedAt(LocalDateTime.now());
        task.setFinishedAt(LocalDateTime.now());
        when(taskMapper.selectById(2L)).thenReturn(task);

        VisionRecognitionCandidate candidate = new VisionRecognitionCandidate();
        candidate.setId(9L);
        candidate.setFoodName("鸡胸肉");
        candidate.setConfidence(BigDecimal.valueOf(0.96));
        candidate.setAmountSuggestion(BigDecimal.valueOf(150));
        candidate.setUnit("克");
        candidate.setCalories(BigDecimal.valueOf(248));
        candidate.setProtein(BigDecimal.valueOf(31));
        candidate.setFat(BigDecimal.valueOf(5));
        candidate.setCarbs(BigDecimal.ZERO);
        when(candidateMapper.findByTaskId(2L)).thenReturn(List.of(candidate));

        Map<String, Object> result = visionRecognitionService.getTask(1L, 2L);

        assertEquals("SUCCESS", result.get("status"));
        assertEquals(1, ((List<?>) result.get("candidates")).size());
    }

    @Test
    void shouldRejectTaskQueryForOtherUser() {
        VisionRecognitionTask task = new VisionRecognitionTask();
        task.setId(3L);
        task.setUserId(2L);
        when(taskMapper.selectById(3L)).thenReturn(task);

        assertThrows(BusinessException.class, () -> visionRecognitionService.getTask(1L, 3L));
    }
}
