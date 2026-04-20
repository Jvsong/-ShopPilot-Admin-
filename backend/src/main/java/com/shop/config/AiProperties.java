package com.shop.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * AI 配置属性
 */
@Data
@Component
@ConfigurationProperties(prefix = "ai")
public class AiProperties {

    /**
     * API 密钥
     */
    private String apiKey;

    /**
     * 模型名称
     */
    private String model = "deepseek-chat";

    /**
     * API 基础地址
     */
    private String baseUrl = "https://api.deepseek.com";

    /**
     * 温度参数 (0.0 ~ 1.0)
     */
    private Double temperature = 0.7;

    /**
     * 最大令牌数
     */
    private Integer maxTokens = 2000;

    /**
     * 是否启用 AI 功能
     */
    private Boolean enabled = true;
}