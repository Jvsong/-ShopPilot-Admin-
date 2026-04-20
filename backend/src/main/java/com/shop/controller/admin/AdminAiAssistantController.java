package com.shop.controller.admin;

import com.shop.dto.request.AiAssistantQueryRequest;
import com.shop.dto.response.AiAssistantResponse;
import com.shop.dto.response.ApiResponse;
import com.shop.service.AiAssistantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

/**
 * AI 智能问讯控制器
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/api/admin/ai-assistant")
@RequiredArgsConstructor
@Tag(name = "AI智能问讯", description = "智能运营建议接口")
public class AdminAiAssistantController {

    private final AiAssistantService aiAssistantService;

    @Operation(summary = "智能问讯", description = "根据销售、库存和订单统计生成可读的运营建议")
    @GetMapping("/ask")
    public ApiResponse<AiAssistantResponse> ask(
            @Parameter(description = "问题类型: HOT_PRODUCTS, RESTOCK_SUGGESTION, ORDER_ALERT, DAILY_SUMMARY")
            @RequestParam(required = false) String questionType,
            @Parameter(description = "时间范围: 7d, 15d, 30d")
            @RequestParam(required = false) String rangeType,
            @Parameter(description = "开始日期")
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @Parameter(description = "结束日期")
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        AiAssistantQueryRequest request = new AiAssistantQueryRequest();
        request.setQuestionType(questionType);
        request.setRangeType(rangeType);
        request.setStartDate(startDate);
        request.setEndDate(endDate);
        return ApiResponse.success(aiAssistantService.ask(request));
    }
}
