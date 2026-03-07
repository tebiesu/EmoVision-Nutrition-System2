package com.project.healthassistant.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.upload")
public class UploadProperties {
    private long maxSizeBytes = 5 * 1024 * 1024;
    private List<String> allowedTypes = List.of("image/jpeg", "image/png", "image/webp");
    private String baseDir = "uploads";
}
