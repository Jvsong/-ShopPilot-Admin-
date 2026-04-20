package com.shop.service;

/**
 * AI 客户端服务
 */
public interface AiClientService {

    /**
     * 生成运营建议
     *
     * @param context  上下文信息（JSON 字符串）
     * @param question 问题类型
     * @return AI 生成的建议文本
     */
    String generateSuggestion(String context, String question);

    /**
     * 生成摘要
     *
     * @param context  上下文信息（JSON 字符串）
     * @param question 问题类型
     * @return AI 生成的摘要文本
     */
    String generateSummary(String context, String question);

    /**
     * 检查 AI 服务是否可用
     */
    boolean isAvailable();
}