package com.project.healthassistant;

import com.project.healthassistant.config.AiAssistantProperties;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableAsync;

@MapperScan("com.project.healthassistant.modules.**.infrastructure")
@ConfigurationPropertiesScan(basePackageClasses = AiAssistantProperties.class)
@EnableAsync
@SpringBootApplication
public class HealthAssistantApplication {

    public static void main(String[] args) {
        SpringApplication.run(HealthAssistantApplication.class, args);
    }
}