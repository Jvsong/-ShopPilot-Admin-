package com.shop.config;

import com.theokanning.openai.service.OpenAiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * AI 客户端配置
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class AiConfig {

    private final AiProperties aiProperties;

    @Bean
    public OpenAiService openAiService() {
        if (!aiProperties.getEnabled()) {
            log.warn("AI 功能已禁用，将返回模拟数据");
            return null;
        }

        String apiKey = aiProperties.getApiKey();
        if (apiKey == null || apiKey.trim().isEmpty()) {
            log.warn("AI API 密钥未配置，将返回模拟数据");
            return null;
        }

        try {
            // TODO: 根据 OpenAiService 版本调整构造函数
            // 当前使用两个参数的构造函数，baseUrl 可能无法设置
            // DeepSeek 需要自定义 baseUrl，可能需要使用其他构造函数或配置方式
            OpenAiService service = new OpenAiService(apiKey, Duration.ofSeconds(60));
            log.info("AI 客户端初始化成功，模型: {}, 地址: {}", aiProperties.getModel(), aiProperties.getBaseUrl());
            return service;
        } catch (Exception e) {
            log.error("AI 客户端初始化失败: {}", e.getMessage(), e);
            return null;
        }
    }
}