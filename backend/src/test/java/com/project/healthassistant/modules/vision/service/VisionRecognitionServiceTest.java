package com.project.healthassistant.modules.vision.service;

import com.project.healthassistant.modules.vision.domain.VisionRecognitionCandidate;
import com.project.healthassistant.modules.vision.domain.VisionRecognitionTask;
import com.project.healthassistant.modules.vision.infrastructure.VisionRecognitionCandidateMapper;
import com.project.healthassistant.modules.vision.infrastructure.VisionRecognitionTaskMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VisionRecognitionServiceTest {

    @Mock
    private VisionRecognitionTaskMapper taskMapper;
    @Mock
    private VisionRecognitionCandidateMapper candidateMapper;

    @InjectMocks
    private VisionRecognitionService visionRecognitionService;

    @Test
    void shouldReturnCandidatesForRecognizableMeal() {
        doAnswer(invocation -> {
            VisionRecognitionTask task = invocation.getArgument(0);
            task.setId(1L);
            return 1;
        }).when(taskMapper).insert(any(VisionRecognitionTask.class));

        Map<String, Object> result = visionRecognitionService.createTask(1L, "/uploads/demo.png", "chicken rice salad");

        assertEquals("SUCCESS", result.get("status"));
        assertFalse(((List<?>) result.get("candidates")).isEmpty());
    }

    @Test
    void shouldReturnEmptyWhenTriggered() {
        doAnswer(invocation -> {
            VisionRecognitionTask task = invocation.getArgument(0);
            task.setId(2L);
            return 1;
        }).when(taskMapper).insert(any(VisionRecognitionTask.class));

        Map<String, Object> result = visionRecognitionService.createTask(1L, "/uploads/demo.png", "empty-recognition");

        assertEquals("EMPTY", result.get("status"));
        assertTrue(((List<?>) result.get("candidates")).isEmpty());
    }
}
