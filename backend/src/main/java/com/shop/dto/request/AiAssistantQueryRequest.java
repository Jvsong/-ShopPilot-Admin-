package com.shop.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;

/**
 * AI 智能问讯请求
 */
@Data
@Schema(description = "AI智能问讯请求")
public class AiAssistantQueryRequest {

    @Schema(description = "问题类型: HOT_PRODUCTS, RESTOCK_SUGGESTION, ORDER_ALERT, DAILY_SUMMARY", example = "DAILY_SUMMARY")
    private String questionType;

    @Schema(description = "时间范围: 7d, 15d, 30d", example = "7d")
    private String rangeType;

    @Schema(description = "自定义开始日期", example = "2026-04-01")
    private LocalDate startDate;

    @Schema(description = "自定义结束日期", example = "2026-04-20")
    private LocalDate endDate;
}
