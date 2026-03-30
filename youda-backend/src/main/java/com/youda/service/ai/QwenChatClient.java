package com.youda.service.ai;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.youda.entity.ChatMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class QwenChatClient {

    @Value("${ai.qwen.api-key:${DASHSCOPE_API_KEY:}}")
    private String apiKey;

    @Value("${ai.qwen.base-url:https://dashscope.aliyuncs.com/compatible-mode/v1}")
    private String baseUrl;

    @Value("${ai.qwen.model:qwen-turbo-latest}")
    private String model;

    @Value("${ai.qwen.temperature:0.3}")
    private Double temperature;

    @Value("${ai.qwen.timeout-ms:60000}")
    private Integer timeoutMs;

    @Value("${ai.qwen.max-history-messages:12}")
    private Integer maxHistoryMessages;

    @Value("${ai.qwen.system-prompt:你是优答平台的中小学学习辅导助手。请使用简体中文回答，先给结论，再给步骤；语言准确、清晰、适合学生理解。如果题目信息不足，要明确指出缺失信息并引导补充。}")
    private String systemPrompt;

    public int getMaxHistoryMessages() {
        return maxHistoryMessages == null || maxHistoryMessages <= 0 ? 12 : maxHistoryMessages;
    }

    public String generateReply(List<ChatMessage> historyMessages) {
        if (StrUtil.isBlank(apiKey)) {
            return "AI 服务尚未配置 DASHSCOPE_API_KEY，暂时无法调用千问。请在后端环境变量中配置后重试。";
        }

        try {
            JSONObject body = JSONUtil.createObj()
                    .set("model", model)
                    .set("temperature", temperature)
                    .set("messages", buildMessages(historyMessages));

            HttpResponse response = HttpRequest.post(normalizeBaseUrl() + "/chat/completions")
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .timeout(timeoutMs)
                    .body(body.toString())
                    .execute();

            String responseBody = response.body();
            if (response.getStatus() >= 400) {
                return extractErrorMessage(response.getStatus(), responseBody);
            }

            JSONObject json = JSONUtil.parseObj(responseBody);
            JSONArray choices = json.getJSONArray("choices");
            if (choices == null || choices.isEmpty()) {
                return "千问返回了空结果，请稍后重试。";
            }

            JSONObject firstChoice = choices.getJSONObject(0);
            if (firstChoice == null) {
                return "千问返回了空结果，请稍后重试。";
            }

            JSONObject message = firstChoice.getJSONObject("message");
            if (message == null) {
                return "千问返回了空结果，请稍后重试。";
            }

            String content = flattenContent(message.get("content"));
            return StrUtil.isBlank(content) ? "千问返回了空内容，请稍后重试。" : content.trim();
        } catch (Exception ex) {
            return "调用千问失败，请检查网络连接、模型名称和 DASHSCOPE_API_KEY 配置。";
        }
    }

    private JSONArray buildMessages(List<ChatMessage> historyMessages) {
        List<ChatMessage> safeHistory = historyMessages == null ? Collections.emptyList() : historyMessages;
        List<ChatMessage> slicedHistory = safeHistory;
        int limit = getMaxHistoryMessages();
        if (safeHistory.size() > limit) {
            slicedHistory = new ArrayList<>(safeHistory.subList(safeHistory.size() - limit, safeHistory.size()));
        }

        JSONArray messages = new JSONArray();
        messages.add(JSONUtil.createObj()
                .set("role", "system")
                .set("content", systemPrompt));

        for (ChatMessage chatMessage : slicedHistory) {
            messages.add(JSONUtil.createObj()
                    .set("role", chatMessage.getRole())
                    .set("content", buildMessageContent(chatMessage)));
        }
        return messages;
    }

    private String buildMessageContent(ChatMessage chatMessage) {
        String content = StrUtil.nullToEmpty(chatMessage.getContent()).trim();
        if (StrUtil.isNotBlank(chatMessage.getImage())) {
            String imageHint = "用户还上传了一张图片，地址为：" + chatMessage.getImage()
                    + "。如果当前模型无法直接理解图片，请明确告知用户需要补充图片文字描述，或切换到支持视觉输入的千问模型。";
            return StrUtil.isBlank(content) ? imageHint : content + "\n\n" + imageHint;
        }
        return StrUtil.blankToDefault(content, "请结合上下文继续回答用户问题。") ;
    }

    private String flattenContent(Object content) {
        if (content == null) {
            return "";
        }
        if (content instanceof CharSequence) {
            return content.toString();
        }
        if (content instanceof JSONArray) {
            JSONArray contentArray = (JSONArray) content;
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < contentArray.size(); i++) {
                Object item = contentArray.get(i);
                if (item instanceof JSONObject) {
                    JSONObject itemObject = (JSONObject) item;
                    String text = itemObject.getStr("text");
                    if (StrUtil.isNotBlank(text)) {
                        if (builder.length() > 0) {
                            builder.append('\n');
                        }
                        builder.append(text);
                    }
                }
            }
            return builder.toString();
        }
        return content.toString();
    }

    private String extractErrorMessage(int statusCode, String responseBody) {
        try {
            JSONObject json = JSONUtil.parseObj(responseBody);
            JSONObject error = json.getJSONObject("error");
            if (error != null && StrUtil.isNotBlank(error.getStr("message"))) {
                return "调用千问失败（HTTP " + statusCode + "）：" + error.getStr("message");
            }
        } catch (Exception ignored) {
        }
        return "调用千问失败（HTTP " + statusCode + "），请检查模型配置或稍后重试。";
    }

    private String normalizeBaseUrl() {
        return StrUtil.removeSuffix(baseUrl, "/");
    }
}
