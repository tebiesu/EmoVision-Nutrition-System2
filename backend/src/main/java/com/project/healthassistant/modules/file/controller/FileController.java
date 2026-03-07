package com.project.healthassistant.modules.file.controller;

import com.project.healthassistant.common.api.ApiResponse;
import com.project.healthassistant.common.security.CurrentUserService;
import com.project.healthassistant.common.trace.TraceIdHolder;
import com.project.healthassistant.modules.file.service.FileStorageService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/files")
public class FileController {

    private final FileStorageService fileStorageService;
    private final CurrentUserService currentUserService;

    public FileController(FileStorageService fileStorageService, CurrentUserService currentUserService) {
        this.fileStorageService = fileStorageService;
        this.currentUserService = currentUserService;
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<Map<String, Object>> upload(@RequestPart("file") MultipartFile file) {
        return ApiResponse.success("upload success", fileStorageService.store(currentUserService.currentUserId(), file), TraceIdHolder.getTraceId());
    }
}
