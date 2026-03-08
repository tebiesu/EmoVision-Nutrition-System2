package com.project.healthassistant.modules.assistant.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.healthassistant.common.api.ResultCode;
import com.project.healthassistant.common.exception.BusinessException;
import com.project.healthassistant.config.AiAssistantProperties;
import com.project.healthassistant.modules.meal.application.MealApplicationService;
import com.project.healthassistant.modules.profile.application.ProfileApplicationService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class NutritionAssistantService {

    private final RestClient.Builder restClientBuilder;
    private final ObjectMapper objectMapper;
    private final AiAssistantProperties aiAssistantProperties;
    private final ProfileApplicationService profileApplicationService;
    private final MealApplicationService mealApplicationService;

    public NutritionAssistantService(RestClient.Builder restClientBuilder,
                                     ObjectMapper objectMapper,
                                     AiAssistantProperties aiAssistantProperties,
                                     ProfileApplicationService profileApplicationService,
                                     MealApplicationService mealApplicationService) {
        this.restClientBuilder = restClientBuilder;
        this.objectMapper = objectMapper;
        this.aiAssistantProperties = aiAssistantProperties;
        this.profileApplicationService = profileApplicationService;
        this.mealApplicationService = mealApplicationService;
    }

    public Map<String, Object> chat(Long userId, String message, ChatContextOptions options) {
        Map<String, Object> profile = loadProfileSafely(userId);
        List<Map<String, Object>> recentMeals = mealApplicationService.listMeals(userId).stream().limit(3).toList();
        Map<String, Object> latestAssessment = recentMeals.isEmpty()
                ? Map.of()
                : castMap(recentMeals.get(0).get("assessment"));
        String context = buildContext(profile, recentMeals, latestAssessment, message, options);
        List<String> sources = buildSources(options, recentMeals, latestAssessment);

        Map<String, Object> result = new LinkedHashMap<>();
        if (!canUseAi()) {
            result.put("usedAi", false);
            result.put("reply", fallbackReply(profile, recentMeals, latestAssessment, message, options));
            result.put("context", context);
            result.put("sources", sources);
            return result;
        }

        try {
            RestClient client = restClientBuilder.baseUrl(normalizeBaseUrl(aiAssistantProperties.getBaseUrl())).build();
            Map<String, Object> payload = Map.of(
                    "model", aiAssistantProperties.getChat().getModel(),
                    "temperature", 0.4,
                    "messages", List.of(
                            Map.of("role", "system", "content", aiAssistantProperties.getChat().getSystemPrompt()),
                            Map.of("role", "user", "content", context)
                    )
            );
            Map<String, Object> response = client.post()
                    .uri("/chat/completions")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + aiAssistantProperties.getApiKey())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(payload)
                    .retrieve()
                    .body(Map.class);

            String reply = extractContent(response);
            result.put("usedAi", true);
            result.put("reply", StringUtils.hasText(reply) ? reply : fallbackReply(profile, recentMeals, latestAssessment, message, options));
            result.put("context", context);
            result.put("sources", sources);
            return result;
        } catch (Exception ex) {
            result.put("usedAi", false);
            result.put("reply", fallbackReply(profile, recentMeals, latestAssessment, message, options));
            result.put("context", context);
            result.put("sources", sources);
            result.put("error", ex.getMessage());
            return result;
        }
    }

    private boolean canUseAi() {
        return aiAssistantProperties.isEnabled()
                && StringUtils.hasText(aiAssistantProperties.getApiKey())
                && StringUtils.hasText(aiAssistantProperties.getBaseUrl());
    }

    private List<String> buildSources(ChatContextOptions options,
                                      List<Map<String, Object>> recentMeals,
                                      Map<String, Object> latestAssessment) {
        List<String> sources = new ArrayList<>();
        if (options.includeProfile()) {
            sources.add("健康档案");
        }
        if (options.includeRecentMeals() && !recentMeals.isEmpty()) {
            sources.add("最近三餐");
        }
        if (options.includeLatestAssessment() && !latestAssessment.isEmpty()) {
            sources.add("最近一次评估");
        }
        if (sources.isEmpty()) {
            sources.add("当前提问");
        }
        return sources;
    }

    private String buildContext(Map<String, Object> profile,
                                List<Map<String, Object>> recentMeals,
                                Map<String, Object> latestAssessment,
                                String message,
                                ChatContextOptions options) {
        try {
            StringBuilder builder = new StringBuilder();
            if (options.includeProfile()) {
                builder.append("用户档案：").append(objectMapper.writeValueAsString(profile)).append('\n');
            }
            if (options.includeRecentMeals()) {
                builder.append("最近三餐：").append(objectMapper.writeValueAsString(recentMeals)).append('\n');
            }
            if (options.includeLatestAssessment() && !latestAssessment.isEmpty()) {
                builder.append("最近一次评估：").append(objectMapper.writeValueAsString(latestAssessment)).append('\n');
            }
            builder.append("用户提问：").append(message).append('\n');
            builder.append("请你以营养助手身份，用简体中文从当前判断、原因说明、下一餐建议、风险提醒四个角度回答。");
            builder.append("如果用户提供了健康档案、饮食记录或评估结果，请显式引用这些信息。");
            return builder.toString();
        } catch (Exception ex) {
            throw new BusinessException(ResultCode.SYSTEM_ERROR.getCode(), "AI 上下文构建失败");
        }
    }

    private String fallbackReply(Map<String, Object> profile,
                                 List<Map<String, Object>> recentMeals,
                                 Map<String, Object> latestAssessment,
                                 String message,
                                 ChatContextOptions options) {
        Object goal = profile.getOrDefault("goal", "maintain");
        int mealCount = recentMeals.size();
        StringBuilder builder = new StringBuilder();
        builder.append("当前 AI 助手未启用，先给你一版规则化建议：");
        builder.append("你的当前目标是 ").append(goal).append("，最近已记录 ").append(mealCount).append(" 条餐次。");
        builder.append("针对“").append(message).append("”，建议先关注总热量、蛋白质摄入和当前情绪状态。");
        if (options.includeLatestAssessment() && !latestAssessment.isEmpty()) {
            builder.append("最近一次评估显示风险等级为 ")
                    .append(latestAssessment.getOrDefault("riskLevel", "未知"))
                    .append("。");
        }
        builder.append("下一餐优先补充蔬菜、优质蛋白和足量饮水。");
        if (!options.includeProfile() || !options.includeRecentMeals()) {
            builder.append("如果想让建议更精准，建议同时携带健康档案和近期饮食记录。");
        }
        return builder.toString();
    }

    private Map<String, Object> loadProfileSafely(Long userId) {
        try {
            return profileApplicationService.get(userId);
        } catch (BusinessException ex) {
            if (ex.getCode() != ResultCode.PROFILE_NOT_FOUND.getCode()) {
                throw ex;
            }
            Map<String, Object> profile = new LinkedHashMap<>();
            profile.put("status", "EMPTY");
            profile.put("tip", "用户暂未完善健康档案");
            return profile;
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> castMap(Object value) {
        return value instanceof Map<?, ?> map ? (Map<String, Object>) map : Map.of();
    }

    private String normalizeBaseUrl(String baseUrl) {
        String trimmed = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        return trimmed.endsWith("/v1") ? trimmed : trimmed + "/v1";
    }

    @SuppressWarnings("unchecked")
    private String extractContent(Map<String, Object> response) {
        if (response == null) {
            return "";
        }
        Object choicesObject = response.get("choices");
        if (!(choicesObject instanceof List<?> choices) || choices.isEmpty()) {
            return "";
        }
        Object first = choices.get(0);
        if (!(first instanceof Map<?, ?> firstMap)) {
            return "";
        }
        Object messageObject = firstMap.get("message");
        if (!(messageObject instanceof Map<?, ?> messageMap)) {
            return "";
        }
        Object content = messageMap.get("content");
        if (content instanceof String text) {
            return text;
        }
        if (content instanceof List<?> blocks) {
            StringBuilder builder = new StringBuilder();
            for (Object block : blocks) {
                if (block instanceof Map<?, ?> part && part.get("text") != null) {
                    builder.append(part.get("text"));
                }
            }
            return builder.toString();
        }
        return content == null ? "" : String.valueOf(content);
    }

    public record ChatContextOptions(boolean includeProfile, boolean includeRecentMeals, boolean includeLatestAssessment) {
    }
}
