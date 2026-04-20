package com.shop.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 补货分析响应
 */
@Data
@Schema(description = "补货分析响应")
public class RestockAnalysisResponse {

    @Schema(description = "开始日期")
    private LocalDate startDate;

    @Schema(description = "结束日期")
    private LocalDate endDate;

    @Schema(description = "分析天数", example = "7")
    private Integer analysisDays;

    @Schema(description = "高风险商品数", example = "2")
    private Integer highRiskCount;

    @Schema(description = "中风险商品数", example = "3")
    private Integer mediumRiskCount;

    @Schema(description = "低风险商品数", example = "5")
    private Integer lowRiskCount;

    @Schema(description = "补货建议列表")
    private List<RestockSuggestion> suggestions;

    @Data
    @Schema(description = "补货建议项")
    public static class RestockSuggestion {

        @Schema(description = "商品ID", example = "1")
        private Long productId;

        @Schema(description = "商品名称", example = "iPhone 15 Pro Max")
        private String productName;

        @Schema(description = "当前库存", example = "12")
        private Integer currentStock;

        @Schema(description = "统计周期销量", example = "18")
        private Integer salesQuantity;

        @Schema(description = "平均日销量", example = "2.57")
        private BigDecimal averageDailySales;

        @Schema(description = "预计可售天数", example = "4.67")
        private BigDecimal estimatedDaysRemaining;

        @Schema(description = "风险等级", example = "HIGH")
        private String riskLevel;

        @Schema(description = "建议补货量", example = "27")
        private Integer suggestedRestockQuantity;

        @Schema(description = "建议说明")
        private String recommendation;
    }
}
