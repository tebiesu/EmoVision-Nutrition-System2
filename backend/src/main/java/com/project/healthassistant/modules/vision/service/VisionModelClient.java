package com.project.healthassistant.modules.vision.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.healthassistant.config.AiAssistantProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class VisionModelClient {

    private final RestClient.Builder restClientBuilder;
    private final ObjectMapper objectMapper;
    private final AiAssistantProperties aiAssistantProperties;

    public VisionModelClient(RestClient.Builder restClientBuilder,
                             ObjectMapper objectMapper,
                             AiAssistantProperties aiAssistantProperties) {
        this.restClientBuilder = restClientBuilder;
        this.objectMapper = objectMapper;
        this.aiAssistantProperties = aiAssistantProperties;
    }

    public VisionRecognitionResult recognize(String imageUrl, String description) {
        if (!canUseAi(imageUrl)) {
            return VisionRecognitionResult.fallback(buildFallbackFoods(description), "AI 未启用或图片地址为空，已回退到人工确认模式");
        }

        try {
            RestClient client = restClientBuilder.baseUrl(normalizeBaseUrl(aiAssistantProperties.getBaseUrl())).build();
            Map<String, Object> response = client.post()
                    .uri("/chat/completions")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + aiAssistantProperties.getApiKey())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(buildPayload(imageUrl, description))
                    .retrieve()
                    .body(Map.class);

            String content = extractContent(response);
            List<RecognizedFood> foods = parseFoods(content);
            if (foods.isEmpty()) {
                return VisionRecognitionResult.fallback(buildFallbackFoods(description), "模型未返回可用候选，已回退到人工确认模式");
            }
            return VisionRecognitionResult.success(foods, content);
        } catch (Exception ex) {
            return VisionRecognitionResult.fallback(buildFallbackFoods(description), ex.getMessage());
        }
    }

    private boolean canUseAi(String imageUrl) {
        return aiAssistantProperties.isEnabled()
                && StringUtils.hasText(aiAssistantProperties.getApiKey())
                && StringUtils.hasText(aiAssistantProperties.getBaseUrl())
                && StringUtils.hasText(imageUrl);
    }

    private Map<String, Object> buildPayload(String imageUrl, String description) throws Exception {
        List<Map<String, Object>> content = new ArrayList<>();
        content.add(Map.of(
                "type", "text",
                "text", buildVisionPrompt(description)
        ));
        content.add(Map.of(
                "type", "image_url",
                "image_url", Map.of("url", resolveImageUrl(imageUrl))
        ));

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("model", aiAssistantProperties.getVision().getModel());
        payload.put("temperature", 0.1);
        payload.put("response_format", Map.of("type", "json_object"));
        payload.put("messages", List.of(
                Map.of("role", "system", "content", aiAssistantProperties.getVision().getSystemPrompt()),
                Map.of("role", "user", "content", content)
        ));
        return payload;
    }

    private String buildVisionPrompt(String description) {
        return "请识别图片中的食物，并返回严格 JSON。"
                + "JSON 顶层必须是 {\"foods\":[...]}。"
                + "每个 food 包含 foodName、amount、unit、calories、protein、fat、carbs、confidence、reason。"
                + "如果图片无法完全判断，请结合描述给出合理估算。"
                + "禁止输出 markdown 代码块，禁止输出额外说明。"
                + "补充描述：" + (StringUtils.hasText(description) ? description : "无");
    }

    private String resolveImageUrl(String imageUrl) throws Exception {
        if (imageUrl.startsWith("http://") || imageUrl.startsWith("https://") || imageUrl.startsWith("data:")) {
            return imageUrl;
        }
        String relativePath = imageUrl.startsWith("/") ? imageUrl.substring(1) : imageUrl;
        Path filePath = Paths.get(relativePath);
        byte[] bytes = Files.readAllBytes(filePath);
        String mimeType = Files.probeContentType(filePath);
        if (!StringUtils.hasText(mimeType)) {
            mimeType = imageUrl.endsWith(".png") ? "image/png" : imageUrl.endsWith(".webp") ? "image/webp" : "image/jpeg";
        }
        return "data:" + mimeType + ";base64," + Base64.getEncoder().encodeToString(bytes);
    }

    private List<RecognizedFood> parseFoods(String content) throws Exception {
        String json = stripMarkdownFence(content);
        if (!StringUtils.hasText(json)) {
            return List.of();
        }
        JsonNode root = objectMapper.readTree(json);
        JsonNode foodsNode = root.has("foods") ? root.get("foods") : root;
        List<RecognizedFood> foods = new ArrayList<>();
        if (foodsNode == null || !foodsNode.isArray()) {
            return foods;
        }
        for (JsonNode node : foodsNode) {
            foods.add(new RecognizedFood(
                    node.path("foodName").asText("待确认食物"),
                    decimal(node.path("confidence"), 0.60),
                    decimal(node.path("amount"), 1),
                    node.path("unit").asText("份"),
                    decimal(node.path("calories"), 180),
                    decimal(node.path("protein"), 8),
                    decimal(node.path("fat"), 6),
                    decimal(node.path("carbs"), 18),
                    node.path("reason").asText("")
            ));
        }
        return foods;
    }

    private BigDecimal decimal(JsonNode node, double defaultValue) {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return BigDecimal.valueOf(defaultValue);
        }
        if (node.isNumber()) {
            return node.decimalValue();
        }
        try {
            return new BigDecimal(node.asText());
        } catch (Exception ex) {
            return BigDecimal.valueOf(defaultValue);
        }
    }

    private String stripMarkdownFence(String content) {
        String trimmed = content == null ? "" : content.trim();
        if (trimmed.startsWith("```") && trimmed.endsWith("```")) {
            String withoutStart = trimmed.replaceFirst("^```(?:json)?", "").trim();
            return withoutStart.substring(0, withoutStart.length() - 3).trim();
        }
        return trimmed;
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

    private String normalizeBaseUrl(String baseUrl) {
        String trimmed = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        return trimmed.endsWith("/v1") ? trimmed : trimmed + "/v1";
    }

    private List<RecognizedFood> buildFallbackFoods(String description) {
        List<String> names = extractFallbackNames(description);
        List<RecognizedFood> foods = new ArrayList<>();
        for (String name : names) {
            foods.add(new RecognizedFood(
                    name,
                    BigDecimal.valueOf(0.25),
                    BigDecimal.ONE,
                    "份",
                    BigDecimal.valueOf(180),
                    BigDecimal.valueOf(8),
                    BigDecimal.valueOf(6),
                    BigDecimal.valueOf(18),
                    "规则兜底结果，请人工确认食物名称、分量和营养值"
            ));
        }
        if (foods.isEmpty()) {
            foods.add(new RecognizedFood(
                    "待确认餐食",
                    BigDecimal.valueOf(0.20),
                    BigDecimal.ONE,
                    "份",
                    BigDecimal.valueOf(180),
                    BigDecimal.valueOf(8),
                    BigDecimal.valueOf(6),
                    BigDecimal.valueOf(18),
                    "AI 暂不可用，已提供低置信候选，请手动核对"
            ));
        }
        return foods;
    }

    private List<String> extractFallbackNames(String description) {
        if (!StringUtils.hasText(description)) {
            return List.of();
        }
        String normalized = description
                .replace('，', ',')
                .replace('、', ',')
                .replace('；', ',')
                .replace('+', ',')
                .replace('/', ',');
        List<String> names = new ArrayList<>();
        for (String token : normalized.split(",")) {
            String name = token.trim();
            if (StringUtils.hasText(name) && names.stream().noneMatch(name::equalsIgnoreCase)) {
                names.add(name);
            }
            if (names.size() >= 4) {
                break;
            }
        }
        return names;
    }

    public record RecognizedFood(String foodName, BigDecimal confidence, BigDecimal amount, String unit,
                                 BigDecimal calories, BigDecimal protein, BigDecimal fat, BigDecimal carbs,
                                 String reason) {
    }

    public record VisionRecognitionResult(boolean usedAi, String rawContent, String message, List<RecognizedFood> foods) {
        static VisionRecognitionResult success(List<RecognizedFood> foods, String rawContent) {
            return new VisionRecognitionResult(true, rawContent, "AI 识别完成", foods);
        }

        static VisionRecognitionResult fallback(List<RecognizedFood> foods, String message) {
            return new VisionRecognitionResult(false, "", message, foods);
        }
    }
}
