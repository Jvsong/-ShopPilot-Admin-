package com.shop.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * AI 智能问讯响应
 */
@Data
@Schema(description = "AI智能问讯响应")
public class AiAssistantResponse {

    @Schema(description = "问题类型", example = "DAILY_SUMMARY")
    private String questionType;

    @Schema(description = "开始日期")
    private LocalDate startDate;

    @Schema(description = "结束日期")
    private LocalDate endDate;

    @Schema(description = "摘要")
    private String summary;

    @Schema(description = "建议列表")
    private List<Suggestion> suggestions;

    @Schema(description = "关键指标")
    private Map<String, Object> metrics;

    @Data
    @Schema(description = "建议项")
    public static class Suggestion {

        @Schema(description = "标题")
        private String title;

        @Schema(description = "等级", example = "HIGH")
        private String level;

        @Schema(description = "详细说明")
        private String detail;

        @Schema(description = "建议动作")
        private String action;

        @Schema(description = "跳转目标", example = "/admin/orders")
        private String actionTarget;

        @Schema(description = "依据")
        private List<String> evidence;
    }
}
