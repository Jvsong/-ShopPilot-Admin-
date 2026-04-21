package com.shop.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shop.config.AiProperties;
import com.shop.service.AiClientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * AI client service implementation.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiClientServiceImpl implements AiClientService {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String SYSTEM_PROMPT =
            "You are a professional ecommerce operations assistant. Please answer in Chinese.";

    private final RestTemplate aiRestTemplate;
    private final AiProperties aiProperties;

    @Override
    public String generateSuggestion(String context, String question) {
        if (!isAvailable()) {
            return null;
        }

        return callAi(buildSuggestionPrompt(context, question));
    }

    @Override
    public String generateSummary(String context, String question) {
        if (!isAvailable()) {
            return null;
        }

        return callAi(buildSummaryPrompt(context, question));
    }

    @Override
    public boolean isAvailable() {
        return Boolean.TRUE.equals(aiProperties.getEnabled()) && StringUtils.hasText(aiProperties.getApiKey());
    }

    private String callAi(String prompt) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(aiProperties.getApiKey().trim());

            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("model", aiProperties.getModel());
            payload.put("messages", buildMessages(prompt));
            payload.put("temperature", aiProperties.getTemperature());
            payload.put("max_tokens", aiProperties.getMaxTokens());

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);
            ResponseEntity<Map> response = aiRestTemplate.exchange(
                    resolveChatCompletionsUrl(),
                    HttpMethod.POST,
                    entity,
                    Map.class
            );

            String result = extractContent(response.getBody());
            if (!StringUtils.hasText(result)) {
                log.warn("AI response body did not contain content: {}", response.getBody());
                return null;
            }

            log.debug("AI response: {}", result);
            return result.trim();
        } catch (RestClientException e) {
            log.error("AI API request failed: {}", e.getMessage(), e);
            return null;
        }
    }

    private List<Map<String, String>> buildMessages(String prompt) {
        List<Map<String, String>> messages = new ArrayList<>();

        Map<String, String> systemMessage = new LinkedHashMap<>();
        systemMessage.put("role", "system");
        systemMessage.put("content", SYSTEM_PROMPT);
        messages.add(systemMessage);

        Map<String, String> userMessage = new LinkedHashMap<>();
        userMessage.put("role", "user");
        userMessage.put("content", prompt);
        messages.add(userMessage);

        return messages;
    }

    @SuppressWarnings("unchecked")
    private String extractContent(Map<String, Object> responseBody) {
        if (responseBody == null) {
            return null;
        }

        Object choicesObj = responseBody.get("choices");
        if (!(choicesObj instanceof List) || ((List<?>) choicesObj).isEmpty()) {
            return null;
        }

        Object firstChoice = ((List<?>) choicesObj).get(0);
        if (!(firstChoice instanceof Map)) {
            return null;
        }

        Object messageObj = ((Map<String, Object>) firstChoice).get("message");
        if (!(messageObj instanceof Map)) {
            return null;
        }

        Object content = ((Map<String, Object>) messageObj).get("content");
        return content == null ? null : String.valueOf(content);
    }

    private String resolveChatCompletionsUrl() {
        String baseUrl = aiProperties.getBaseUrl();
        if (!StringUtils.hasText(baseUrl)) {
            baseUrl = "https://api.deepseek.com";
        }

        String normalized = baseUrl.trim().replaceAll("/+$", "");
        if (normalized.endsWith("/chat/completions")) {
            return normalized;
        }
        if (normalized.endsWith("/v1")) {
            return normalized + "/chat/completions";
        }
        return normalized + "/chat/completions";
    }

    private String buildSuggestionPrompt(String context, String question) {
        try {
            Map<?, ?> contextMap = OBJECT_MAPPER.readValue(context, Map.class);
            String contextJson = OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(contextMap);

            return String.format(
                    "Based on the following ecommerce data, generate practical operations suggestions.%n" +
                            "Question type: %s%n%n" +
                            "Data:%n%s%n%n" +
                            "Requirements:%n" +
                            "1. Keep suggestions concrete and actionable.%n" +
                            "2. Prioritize high-risk issues.%n" +
                            "3. Use ecommerce operations terminology.%n" +
                            "4. Each suggestion should include a clear next action.%n" +
                            "5. Output plain text only, no markdown.%n" +
                            "6. Answer in Chinese.%n%n" +
                            "Generate 2 to 3 core suggestions.",
                    question,
                    contextJson
            );
        } catch (JsonProcessingException e) {
            log.error("Failed to parse AI context JSON: {}", context, e);
            return String.format(
                    "Generate professional ecommerce operation suggestions.%n" +
                            "Question type: %s%n" +
                            "Raw data: %s%n%n" +
                            "Generate 2 to 3 core suggestions in Chinese.",
                    question,
                    context
            );
        }
    }

    private String buildSummaryPrompt(String context, String question) {
        try {
            Map<?, ?> contextMap = OBJECT_MAPPER.readValue(context, Map.class);
            String contextJson = OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(contextMap);

            return String.format(
                    "Based on the following ecommerce data, generate a concise operations summary.%n" +
                            "Question type: %s%n%n" +
                            "Data:%n%s%n%n" +
                            "Requirements:%n" +
                            "1. Highlight the most important metrics.%n" +
                            "2. Mention the main trend and risk.%n" +
                            "3. Keep the summary within 150 Chinese characters if possible.%n" +
                            "4. Use ecommerce operations terminology.%n" +
                            "5. Output plain text only, no markdown.%n" +
                            "6. Answer in Chinese.%n%n" +
                            "Operations summary:",
                    question,
                    contextJson
            );
        } catch (JsonProcessingException e) {
            log.error("Failed to parse AI context JSON: {}", context, e);
            return String.format(
                    "Generate an ecommerce operations summary.%n" +
                            "Question type: %s%n" +
                            "Raw data: %s%n%n" +
                            "Generate a concise Chinese summary.",
                    question,
                    context
            );
        }
    }
}
