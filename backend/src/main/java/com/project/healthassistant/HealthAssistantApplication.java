package com.project.healthassistant;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@MapperScan("com.project.healthassistant.modules.**.infrastructure")
@ConfigurationPropertiesScan
@SpringBootApplication
public class HealthAssistantApplication {

    public static void main(String[] args) {
        SpringApplication.run(HealthAssistantApplication.class, args);
    }
}
