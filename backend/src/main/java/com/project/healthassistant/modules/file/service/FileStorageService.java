package com.project.healthassistant.modules.file.service;

import com.project.healthassistant.common.api.ResultCode;
import com.project.healthassistant.common.exception.BusinessException;
import com.project.healthassistant.config.UploadProperties;
import com.project.healthassistant.modules.file.domain.UploadFileRecord;
import com.project.healthassistant.modules.file.infrastructure.UploadFileMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class FileStorageService {

    private final UploadProperties uploadProperties;
    private final UploadFileMapper uploadFileMapper;

    public FileStorageService(UploadProperties uploadProperties, UploadFileMapper uploadFileMapper) {
        this.uploadProperties = uploadProperties;
        this.uploadFileMapper = uploadFileMapper;
    }

    public Map<String, Object> store(Long userId, MultipartFile file) {
        validate(file);
        try {
            Path baseDir = Paths.get(uploadProperties.getBaseDir());
            Files.createDirectories(baseDir);
            String extension = file.getOriginalFilename() != null && file.getOriginalFilename().contains(".")
                    ? file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf('.')) : ".bin";
            String fileName = UUID.randomUUID() + extension;
            Path target = baseDir.resolve(fileName);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

            UploadFileRecord record = new UploadFileRecord();
            record.setUserId(userId);
            record.setOriginalName(file.getOriginalFilename());
            record.setStoragePath(target.toString().replace('\\', '/'));
            record.setContentType(file.getContentType());
            record.setSizeBytes(file.getSize());
            record.setCreatedAt(LocalDateTime.now());
            uploadFileMapper.insert(record);

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("fileId", record.getId());
            result.put("url", "/" + record.getStoragePath());
            result.put("contentType", record.getContentType());
            result.put("sizeBytes", record.getSizeBytes());
            return result;
        } catch (IOException ex) {
            throw new BusinessException(ResultCode.SYSTEM_ERROR);
        }
    }

    private void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ResultCode.INVALID_PARAM.getCode(), "File is required");
        }
        if (!uploadProperties.getAllowedTypes().contains(file.getContentType())) {
            throw new BusinessException(ResultCode.INVALID_PARAM.getCode(), "Unsupported file type");
        }
        if (file.getSize() > uploadProperties.getMaxSizeBytes()) {
            throw new BusinessException(ResultCode.INVALID_PARAM.getCode(), "File too large");
        }
    }
}
