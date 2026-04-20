package com.shop.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shop.config.AiProperties;
import com.shop.service.AiClientService;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;
import com.theokanning.openai.service.OpenAiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * AI 客户端服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiClientServiceImpl implements AiClientService {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final OpenAiService openAiService;
    private final AiProperties aiProperties;

    @Override
    public String generateSuggestion(String context, String question) {
        if (!isAvailable()) {
            return null;
        }

        String prompt = buildSuggestionPrompt(context, question);
        return callAi(prompt);
    }

    @Override
    public String generateSummary(String context, String question) {
        if (!isAvailable()) {
            return null;
        }

        String prompt = buildSummaryPrompt(context, question);
        return callAi(prompt);
    }

    @Override
    public boolean isAvailable() {
        return openAiService != null && aiProperties.getEnabled();
    }

    private String callAi(String prompt) {
        try {
            List<ChatMessage> messages = new ArrayList<>();
            messages.add(new ChatMessage(ChatMessageRole.SYSTEM.value(), "你是一个专业的电商运营助手，请用中文回答。"));
            messages.add(new ChatMessage(ChatMessageRole.USER.value(), prompt));

            ChatCompletionRequest request = ChatCompletionRequest.builder()
                    .model(aiProperties.getModel())
                    .messages(messages)
                    .temperature(aiProperties.getTemperature())
                    .maxTokens(aiProperties.getMaxTokens())
                    .build();

            StringBuilder response = new StringBuilder();
            openAiService.createChatCompletion(request)
                    .getChoices()
                    .forEach(choice -> response.append(choice.getMessage().getContent()));

            String result = response.toString().trim();
            log.debug("AI 响应: {}", result);
            return result;
        } catch (Exception e) {
            log.error("调用 AI API 失败: {}", e.getMessage(), e);
            return null;
        }
    }

    private String buildSuggestionPrompt(String context, String question) {
        try {
            Map<?, ?> contextMap = OBJECT_MAPPER.readValue(context, Map.class);
            String contextJson = OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(contextMap);

            return String.format(
                "请根据以下电商运营数据，生成专业、实用的运营建议。\n" +
                "问题类型: %s\n\n" +
                "数据:\n%s\n\n" +
                "要求:\n" +
                "1. 建议内容要具体、可操作\n" +
                "2. 优先关注高风险问题\n" +
                "3. 使用电商运营专业术语\n" +
                "4. 每条建议包含具体的行动指引\n" +
                "5. 输出格式为纯文本，无需 markdown\n\n" +
                "请生成 2-3 条核心建议:", question, contextJson);
        } catch (JsonProcessingException e) {
            log.error("解析上下文 JSON 失败: {}", context, e);
            return String.format(
                "请根据电商运营数据生成专业建议。\n" +
                "问题类型: %s\n" +
                "原始数据: %s\n\n" +
                "请生成 2-3 条核心运营建议:", question, context);
        }
    }

    private String buildSummaryPrompt(String context, String question) {
        try {
            Map<?, ?> contextMap = OBJECT_MAPPER.readValue(context, Map.class);
            String contextJson = OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(contextMap);

            return String.format(
                "请根据以下电商运营数据，生成一段简洁、专业的运营摘要。\n" +
                "问题类型: %s\n\n" +
                "数据:\n%s\n\n" +
                "要求:\n" +
                "1. 突出关键数据指标\n" +
                "2. 指出主要趋势和风险\n" +
                "3. 语言精炼，控制在 150 字以内\n" +
                "4. 使用电商运营专业术语\n" +
                "5. 输出格式为纯文本，无需 markdown\n\n" +
                "运营摘要:", question, contextJson);
        } catch (JsonProcessingException e) {
            log.error("解析上下文 JSON 失败: {}", context, e);
            return String.format(
                "请根据电商运营数据生成运营摘要。\n" +
                "问题类型: %s\n" +
                "原始数据: %s\n\n" +
                "请生成简洁的运营摘要:", question, context);
        }
    }
}