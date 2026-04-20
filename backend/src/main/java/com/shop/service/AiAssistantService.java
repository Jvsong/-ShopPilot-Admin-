package com.shop.service;

import com.shop.dto.request.AiAssistantQueryRequest;
import com.shop.dto.response.AiAssistantResponse;

/**
 * AI 智能问讯服务
 */
public interface AiAssistantService {

    /**
     * 获取智能问讯结果
     */
    AiAssistantResponse ask(AiAssistantQueryRequest request);
}
