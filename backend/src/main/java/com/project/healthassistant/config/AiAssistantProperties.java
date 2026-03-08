package com.project.healthassistant.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.ai")
public class AiAssistantProperties {

    private boolean enabled = false;
    private String channelName = "new-api";
    private String baseUrl = "";
    private String apiKey = "";
    private int timeoutSeconds = 40;
    private Chat chat = new Chat();
    private Vision vision = new Vision();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getChannelName() {
        return channelName;
    }

    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public int getTimeoutSeconds() {
        return timeoutSeconds;
    }

    public void setTimeoutSeconds(int timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
    }

    public Chat getChat() {
        return chat;
    }

    public void setChat(Chat chat) {
        this.chat = chat;
    }

    public Vision getVision() {
        return vision;
    }

    public void setVision(Vision vision) {
        this.vision = vision;
    }

    public static class Chat {
        private String model = "gpt-4o-mini";
        private String systemPrompt = "你是系统内置的营养助手。"
                + "你的角色是谨慎、温和、结构化的饮食与情绪管理顾问。"
                + "回答必须使用简体中文，优先依据用户档案、近期饮食、情绪状态和系统规则给出建议。"
                + "不要做疾病诊断，不要虚构未提供的数据。"
                + "输出时优先包含：当前判断、原因说明、下一餐建议、风险提醒。";

        public String getModel() {
            return model;
        }

        public void setModel(String model) {
            this.model = model;
        }

        public String getSystemPrompt() {
            return systemPrompt;
        }

        public void setSystemPrompt(String systemPrompt) {
            this.systemPrompt = systemPrompt;
        }
    }

    public static class Vision {
        private String model = "gpt-4.1-mini";
        private String systemPrompt = "你是食物识别与营养估算助手。"
                + "请根据图片和文字描述识别食物，并严格输出 JSON。"
                + "顶层必须包含 foods 数组。"
                + "每个 food 必须包含 foodName、amount、unit、calories、protein、fat、carbs、confidence、reason。"
                + "如果不确定，请降低 confidence，并在 reason 中说明原因。"
                + "营养值单位统一为克和千卡。";

        public String getModel() {
            return model;
        }

        public void setModel(String model) {
            this.model = model;
        }

        public String getSystemPrompt() {
            return systemPrompt;
        }

        public void setSystemPrompt(String systemPrompt) {
            this.systemPrompt = systemPrompt;
        }
    }
}
