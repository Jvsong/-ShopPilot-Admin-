package com.shop.service.impl;

import com.shop.dto.request.AiAssistantQueryRequest;
import com.shop.dto.response.AiAssistantResponse;
import com.shop.dto.response.OrderStatisticsResponse;
import com.shop.dto.response.RestockAnalysisResponse;
import com.shop.exception.BusinessException;
import com.shop.exception.ErrorCode;
import com.shop.service.AiAssistantService;
import com.shop.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shop.service.AiClientService;

/**
 * AI 智能问讯服务实现
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AiAssistantServiceImpl implements AiAssistantService {

    private static final String TYPE_HOT_PRODUCTS = "HOT_PRODUCTS";
    private static final String TYPE_RESTOCK_SUGGESTION = "RESTOCK_SUGGESTION";
    private static final String TYPE_ORDER_ALERT = "ORDER_ALERT";
    private static final String TYPE_DAILY_SUMMARY = "DAILY_SUMMARY";

    private final OrderService orderService;
    private final AiClientService aiClientService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public AiAssistantResponse ask(AiAssistantQueryRequest request) {
        LocalDate[] dateRange = resolveDateRange(request);
        LocalDate startDate = dateRange[0];
        LocalDate endDate = dateRange[1];
        String questionType = normalizeQuestionType(request.getQuestionType());

        OrderStatisticsResponse statistics = orderService.getOrderStatistics(startDate, endDate);
        RestockAnalysisResponse restockAnalysis = orderService.getRestockAnalysis(startDate, endDate);

        switch (questionType) {
            case TYPE_HOT_PRODUCTS:
                return buildHotProductsResponse(startDate, endDate, statistics);
            case TYPE_RESTOCK_SUGGESTION:
                return buildRestockResponse(startDate, endDate, restockAnalysis);
            case TYPE_ORDER_ALERT:
                return buildOrderAlertResponse(startDate, endDate, statistics);
            case TYPE_DAILY_SUMMARY:
                return buildDailySummaryResponse(startDate, endDate, statistics, restockAnalysis);
            default:
                throw new BusinessException(ErrorCode.PARAM_ERROR, "不支持的问题类型");
        }
    }

    private AiAssistantResponse buildHotProductsResponse(LocalDate startDate, LocalDate endDate, OrderStatisticsResponse statistics) {
        List<AiAssistantResponse.Suggestion> suggestions = new ArrayList<>();
        List<OrderStatisticsResponse.ProductStatistic> topProducts = statistics.getTopProducts();

        // 构建空的补货分析用于上下文
        RestockAnalysisResponse emptyRestockAnalysis = new RestockAnalysisResponse();
        emptyRestockAnalysis.setHighRiskCount(0);
        emptyRestockAnalysis.setMediumRiskCount(0);
        emptyRestockAnalysis.setLowRiskCount(0);
        emptyRestockAnalysis.setSuggestions(new ArrayList<>());

        // 构建 AI 上下文
        String context = buildContext(statistics, emptyRestockAnalysis, TYPE_HOT_PRODUCTS);
        boolean aiAvailable = aiClientService.isAvailable();

        if (topProducts == null || topProducts.isEmpty()) {
            String detail = aiAvailable ?
                generateAiSuggestionDetail(context, TYPE_HOT_PRODUCTS, "暂无热销商品", "LOW") :
                "当前统计周期内暂无已完成订单商品数据，暂时无法生成热销建议。";
            if (detail.isEmpty()) {
                detail = "当前统计周期内暂无已完成订单商品数据，暂时无法生成热销建议。";
            }
            suggestions.add(buildSuggestion(
                    "暂无热销商品",
                    "LOW",
                    detail,
                    "查看订单",
                    "/admin/orders",
                    Arrays.asList("已完成订单数较少", "热销商品统计为空")
            ));
        } else {
            for (int i = 0; i < Math.min(3, topProducts.size()); i++) {
                OrderStatisticsResponse.ProductStatistic product = topProducts.get(i);
                String title = "热销商品推荐：" + product.getProductName();
                String level = i == 0 ? "HIGH" : "MEDIUM";

                String detail;
                if (aiAvailable) {
                    detail = generateAiSuggestionDetail(context, TYPE_HOT_PRODUCTS, title, level);
                    if (detail.isEmpty()) {
                        detail = String.format("%s 在统计周期内售出 %d 件，销售额 %s，建议优先关注库存并提高曝光。",
                                product.getProductName(),
                                safeInt(product.getSalesQuantity()),
                                formatAmount(product.getSalesAmount()));
                    }
                } else {
                    detail = String.format("%s 在统计周期内售出 %d 件，销售额 %s，建议优先关注库存并提高曝光。",
                            product.getProductName(),
                            safeInt(product.getSalesQuantity()),
                            formatAmount(product.getSalesAmount()));
                }

                suggestions.add(buildSuggestion(
                        title,
                        level,
                        detail,
                        "查看商品",
                        "/admin/products",
                        Arrays.asList(
                                "销量：" + safeInt(product.getSalesQuantity()),
                                "销售额：" + formatAmount(product.getSalesAmount())
                        )
                ));
            }
        }

        AiAssistantResponse response = baseResponse(TYPE_HOT_PRODUCTS, startDate, endDate);

        // 生成摘要
        String summary;
        if (aiAvailable) {
            summary = generateAiSummary(context, TYPE_HOT_PRODUCTS);
            if (summary.isEmpty()) {
                summary = buildHotSummary(topProducts);
            }
        } else {
            summary = buildHotSummary(topProducts);
        }
        response.setSummary(summary);

        response.setSuggestions(suggestions);
        response.setMetrics(buildHotMetrics(statistics));
        return response;
    }

    private AiAssistantResponse buildRestockResponse(LocalDate startDate, LocalDate endDate, RestockAnalysisResponse restockAnalysis) {
        List<AiAssistantResponse.Suggestion> suggestions = new ArrayList<>();
        List<RestockAnalysisResponse.RestockSuggestion> sourceSuggestions = restockAnalysis.getSuggestions();

        // 构建空的订单统计用于上下文
        OrderStatisticsResponse emptyStatistics = new OrderStatisticsResponse();
        emptyStatistics.setTotalOrders(0L);
        emptyStatistics.setTotalSales(BigDecimal.ZERO);
        emptyStatistics.setTopProducts(new ArrayList<>());

        // 构建 AI 上下文
        String context = buildContext(emptyStatistics, restockAnalysis, TYPE_RESTOCK_SUGGESTION);
        boolean aiAvailable = aiClientService.isAvailable();

        if (sourceSuggestions == null || sourceSuggestions.isEmpty()) {
            String detail = aiAvailable ?
                generateAiSuggestionDetail(context, TYPE_RESTOCK_SUGGESTION, "暂无补货风险", "LOW") :
                "当前统计周期内未发现需要优先补货的商品，库存风险整体稳定。";
            if (detail.isEmpty()) {
                detail = "当前统计周期内未发现需要优先补货的商品，库存风险整体稳定。";
            }
            suggestions.add(buildSuggestion(
                    "暂无补货风险",
                    "LOW",
                    detail,
                    "查看商品",
                    "/admin/products",
                    Arrays.asList("高风险商品：0", "中风险商品：0")
            ));
        } else {
            for (RestockAnalysisResponse.RestockSuggestion item : sourceSuggestions) {
                if (!"HIGH".equals(item.getRiskLevel()) && !"MEDIUM".equals(item.getRiskLevel())) {
                    continue;
                }
                String title = "补货建议：" + item.getProductName();
                String level = item.getRiskLevel();

                String detail;
                if (aiAvailable) {
                    detail = generateAiSuggestionDetail(context, TYPE_RESTOCK_SUGGESTION, title, level);
                    if (detail.isEmpty()) {
                        detail = item.getRecommendation();
                    }
                } else {
                    detail = item.getRecommendation();
                }

                suggestions.add(buildSuggestion(
                        title,
                        level,
                        detail,
                        "查看商品库存",
                        "/admin/products",
                        Arrays.asList(
                                "当前库存：" + safeInt(item.getCurrentStock()),
                                "周期销量：" + safeInt(item.getSalesQuantity()),
                                "建议补货：" + safeInt(item.getSuggestedRestockQuantity())
                        )
                ));
                if (suggestions.size() >= 3) {
                    break;
                }
            }
            if (suggestions.isEmpty()) {
                RestockAnalysisResponse.RestockSuggestion first = sourceSuggestions.get(0);
                String title = "库存整体稳定";
                String level = "LOW";
                String detail = aiAvailable ?
                    generateAiSuggestionDetail(context, TYPE_RESTOCK_SUGGESTION, title, level) :
                    "当前商品库存可支撑近期销售，暂无高优先级补货项。";
                if (detail.isEmpty()) {
                    detail = "当前商品库存可支撑近期销售，暂无高优先级补货项。";
                }
                suggestions.add(buildSuggestion(
                        title,
                        level,
                        detail,
                        "查看商品",
                        "/admin/products",
                        Arrays.asList(
                                "低风险商品：" + safeInt(restockAnalysis.getLowRiskCount()),
                                "参考商品：" + first.getProductName()
                        )
                ));
            }
        }

        AiAssistantResponse response = baseResponse(TYPE_RESTOCK_SUGGESTION, startDate, endDate);

        // 生成摘要
        String summary;
        if (aiAvailable) {
            summary = generateAiSummary(context, TYPE_RESTOCK_SUGGESTION);
            if (summary.isEmpty()) {
                summary = buildRestockSummary(restockAnalysis);
            }
        } else {
            summary = buildRestockSummary(restockAnalysis);
        }
        response.setSummary(summary);

        response.setSuggestions(suggestions);
        response.setMetrics(buildRestockMetrics(restockAnalysis));
        return response;
    }

    private AiAssistantResponse buildOrderAlertResponse(LocalDate startDate, LocalDate endDate, OrderStatisticsResponse statistics) {
        List<AiAssistantResponse.Suggestion> suggestions = new ArrayList<>();
        long totalOrders = safeLong(statistics.getTotalOrders());
        long pendingShipment = safeLong(statistics.getPendingShipmentCount());
        long pendingPayment = safeLong(statistics.getPendingPaymentCount());
        long cancelled = safeLong(statistics.getCancelledCount());

        if (totalOrders > 0 && pendingShipment >= 3 && ratio(pendingShipment, totalOrders).compareTo(new BigDecimal("0.30")) >= 0) {
            suggestions.add(buildSuggestion(
                    "待发货订单偏多",
                    "HIGH",
                    String.format("当前待发货订单 %d 单，占比 %s，建议优先处理履约与发货环节。", pendingShipment, formatPercent(ratio(pendingShipment, totalOrders))),
                    "查看订单",
                    "/admin/orders",
                    Arrays.asList(
                            "待发货订单：" + pendingShipment,
                            "订单总数：" + totalOrders
                    )
            ));
        }

        if (totalOrders > 0 && cancelled >= 2 && ratio(cancelled, totalOrders).compareTo(new BigDecimal("0.20")) >= 0) {
            suggestions.add(buildSuggestion(
                    "取消订单占比偏高",
                    "MEDIUM",
                    String.format("当前取消订单 %d 单，占比 %s，建议排查价格、库存或履约体验问题。", cancelled, formatPercent(ratio(cancelled, totalOrders))),
                    "查看订单",
                    "/admin/orders",
                    Arrays.asList(
                            "取消订单：" + cancelled,
                            "取消率：" + formatPercent(ratio(cancelled, totalOrders))
                    )
            ));
        }

        if (totalOrders > 0 && pendingPayment >= 3 && ratio(pendingPayment, totalOrders).compareTo(new BigDecimal("0.25")) >= 0) {
            suggestions.add(buildSuggestion(
                    "待付款订单积压",
                    "MEDIUM",
                    String.format("当前待付款订单 %d 单，占比 %s，可考虑优化催付或限时促单策略。", pendingPayment, formatPercent(ratio(pendingPayment, totalOrders))),
                    "查看订单",
                    "/admin/orders",
                    Arrays.asList(
                            "待付款订单：" + pendingPayment,
                            "待付款占比：" + formatPercent(ratio(pendingPayment, totalOrders))
                    )
            ));
        }

        if (suggestions.isEmpty()) {
            suggestions.add(buildSuggestion(
                    "订单状态整体稳定",
                    "LOW",
                    "当前统计周期内未发现明显异常订单结构，待付款、待发货和取消订单占比均在可接受范围内。",
                    "查看订单",
                    "/admin/orders",
                    Arrays.asList(
                            "订单总数：" + totalOrders,
                            "已完成订单：" + safeLong(statistics.getCompletedCount())
                    )
            ));
        }

        AiAssistantResponse response = baseResponse(TYPE_ORDER_ALERT, startDate, endDate);
        response.setSummary(buildOrderAlertSummary(statistics, suggestions));
        response.setSuggestions(suggestions);
        response.setMetrics(buildOrderMetrics(statistics));
        return response;
    }

    private AiAssistantResponse buildDailySummaryResponse(LocalDate startDate, LocalDate endDate,
                                                          OrderStatisticsResponse statistics,
                                                          RestockAnalysisResponse restockAnalysis) {
        List<AiAssistantResponse.Suggestion> suggestions = new ArrayList<>();

        List<OrderStatisticsResponse.ProductStatistic> topProducts = statistics.getTopProducts();
        if (topProducts != null && !topProducts.isEmpty()) {
            OrderStatisticsResponse.ProductStatistic top = topProducts.get(0);
            suggestions.add(buildSuggestion(
                    "热销机会：" + top.getProductName(),
                    "HIGH",
                    String.format("%s 是当前周期最热销商品，售出 %d 件，建议继续提高曝光和库存保障。",
                            top.getProductName(),
                            safeInt(top.getSalesQuantity())),
                    "查看商品",
                    "/admin/products",
                    Arrays.asList(
                            "销量：" + safeInt(top.getSalesQuantity()),
                            "销售额：" + formatAmount(top.getSalesAmount())
                    )
            ));
        }

        RestockAnalysisResponse.RestockSuggestion urgentRestock = findUrgentRestock(restockAnalysis);
        if (urgentRestock != null) {
            suggestions.add(buildSuggestion(
                    "补货提醒：" + urgentRestock.getProductName(),
                    urgentRestock.getRiskLevel(),
                    urgentRestock.getRecommendation(),
                    "查看商品库存",
                    "/admin/products",
                    Arrays.asList(
                            "当前库存：" + safeInt(urgentRestock.getCurrentStock()),
                            "建议补货：" + safeInt(urgentRestock.getSuggestedRestockQuantity())
                    )
            ));
        }

        AiAssistantResponse orderAlert = buildOrderAlertResponse(startDate, endDate, statistics);
        if (orderAlert.getSuggestions() != null && !orderAlert.getSuggestions().isEmpty()) {
            AiAssistantResponse.Suggestion firstAlert = orderAlert.getSuggestions().get(0);
            if (!"LOW".equals(firstAlert.getLevel()) || suggestions.isEmpty()) {
                suggestions.add(firstAlert);
            }
        }

        if (suggestions.isEmpty()) {
            suggestions.add(buildSuggestion(
                    "经营状态平稳",
                    "LOW",
                    "当前周期内销售、库存和订单结构整体稳定，可继续观察趋势变化。",
                    "查看仪表盘",
                    "/admin/dashboard",
                    Arrays.asList(
                            "订单总数：" + safeLong(statistics.getTotalOrders()),
                            "销售额：" + formatAmount(statistics.getTotalSales())
                    )
            ));
        }

        AiAssistantResponse response = baseResponse(TYPE_DAILY_SUMMARY, startDate, endDate);
        response.setSummary(buildDailySummary(statistics, restockAnalysis));
        response.setSuggestions(suggestions.subList(0, Math.min(3, suggestions.size())));
        response.setMetrics(buildDailyMetrics(statistics, restockAnalysis));
        return response;
    }

    private AiAssistantResponse baseResponse(String questionType, LocalDate startDate, LocalDate endDate) {
        AiAssistantResponse response = new AiAssistantResponse();
        response.setQuestionType(questionType);
        response.setStartDate(startDate);
        response.setEndDate(endDate);
        return response;
    }

    private AiAssistantResponse.Suggestion buildSuggestion(String title, String level, String detail,
                                                           String action, String actionTarget, List<String> evidence) {
        AiAssistantResponse.Suggestion suggestion = new AiAssistantResponse.Suggestion();
        suggestion.setTitle(title);
        suggestion.setLevel(level);
        suggestion.setDetail(detail);
        suggestion.setAction(action);
        suggestion.setActionTarget(actionTarget);
        suggestion.setEvidence(evidence);
        return suggestion;
    }

    private LocalDate[] resolveDateRange(AiAssistantQueryRequest request) {
        LocalDate endDate = request.getEndDate() != null ? request.getEndDate() : LocalDate.now();
        if (request.getStartDate() != null) {
            if (request.getStartDate().isAfter(endDate)) {
                throw new BusinessException(ErrorCode.PARAM_ERROR, "开始日期不能晚于结束日期");
            }
            return new LocalDate[]{request.getStartDate(), endDate};
        }

        String rangeType = StringUtils.hasText(request.getRangeType()) ? request.getRangeType().trim().toLowerCase(Locale.ROOT) : "7d";
        int days;
        switch (rangeType) {
            case "15d":
                days = 15;
                break;
            case "30d":
                days = 30;
                break;
            case "7d":
            default:
                days = 7;
                break;
        }
        return new LocalDate[]{endDate.minusDays(days - 1L), endDate};
    }

    private String normalizeQuestionType(String questionType) {
        if (!StringUtils.hasText(questionType)) {
            return TYPE_DAILY_SUMMARY;
        }
        return questionType.trim().toUpperCase(Locale.ROOT);
    }

    private String buildHotSummary(List<OrderStatisticsResponse.ProductStatistic> topProducts) {
        if (topProducts == null || topProducts.isEmpty()) {
            return "当前统计周期内暂无可识别的热销商品，建议先积累已完成订单数据。";
        }
        OrderStatisticsResponse.ProductStatistic top = topProducts.get(0);
        return String.format("当前最热销的商品是 %s，累计售出 %d 件，建议优先保障库存并增加推荐曝光。",
                top.getProductName(), safeInt(top.getSalesQuantity()));
    }

    private String buildRestockSummary(RestockAnalysisResponse restockAnalysis) {
        int high = safeInt(restockAnalysis.getHighRiskCount());
        int medium = safeInt(restockAnalysis.getMediumRiskCount());
        if (high > 0) {
            return String.format("当前有 %d 个高风险补货项和 %d 个中风险补货项，建议优先处理高风险商品。", high, medium);
        }
        if (medium > 0) {
            return String.format("当前暂无高风险缺货商品，但有 %d 个中风险补货项，建议提前安排库存。", medium);
        }
        return "当前库存风险整体稳定，暂无急需补货的商品。";
    }

    private String buildOrderAlertSummary(OrderStatisticsResponse statistics, List<AiAssistantResponse.Suggestion> suggestions) {
        if (suggestions.isEmpty() || "LOW".equals(suggestions.get(0).getLevel())) {
            return "当前订单结构整体平稳，未发现明显异常。";
        }
        return String.format("当前订单结构存在需要关注的风险，共识别出 %d 条重点提醒，建议优先处理待发货和取消率问题。",
                suggestions.size());
    }

    private String buildDailySummary(OrderStatisticsResponse statistics, RestockAnalysisResponse restockAnalysis) {
        StringBuilder summary = new StringBuilder();
        summary.append("统计周期内共 ").append(safeLong(statistics.getTotalOrders())).append(" 单，销售额 ")
                .append(formatAmount(statistics.getTotalSales())).append("。");
        if (statistics.getTopProducts() != null && !statistics.getTopProducts().isEmpty()) {
            OrderStatisticsResponse.ProductStatistic top = statistics.getTopProducts().get(0);
            summary.append(" 热销商品为 ").append(top.getProductName()).append("，售出 ")
                    .append(safeInt(top.getSalesQuantity())).append(" 件。");
        }
        int highRiskCount = safeInt(restockAnalysis.getHighRiskCount());
        if (highRiskCount > 0) {
            summary.append(" 当前有 ").append(highRiskCount).append(" 个高风险补货项，需要优先处理。");
        } else {
            summary.append(" 当前库存风险整体可控。");
        }
        return summary.toString();
    }

    private Map<String, Object> buildHotMetrics(OrderStatisticsResponse statistics) {
        Map<String, Object> metrics = new LinkedHashMap<>();
        metrics.put("totalOrders", safeLong(statistics.getTotalOrders()));
        metrics.put("totalSales", statistics.getTotalSales());
        metrics.put("topProductCount", statistics.getTopProducts() == null ? 0 : statistics.getTopProducts().size());
        return metrics;
    }

    private Map<String, Object> buildRestockMetrics(RestockAnalysisResponse restockAnalysis) {
        Map<String, Object> metrics = new LinkedHashMap<>();
        metrics.put("analysisDays", safeInt(restockAnalysis.getAnalysisDays()));
        metrics.put("highRiskCount", safeInt(restockAnalysis.getHighRiskCount()));
        metrics.put("mediumRiskCount", safeInt(restockAnalysis.getMediumRiskCount()));
        metrics.put("lowRiskCount", safeInt(restockAnalysis.getLowRiskCount()));
        return metrics;
    }

    private Map<String, Object> buildOrderMetrics(OrderStatisticsResponse statistics) {
        Map<String, Object> metrics = new LinkedHashMap<>();
        long totalOrders = safeLong(statistics.getTotalOrders());
        metrics.put("totalOrders", totalOrders);
        metrics.put("pendingPaymentCount", safeLong(statistics.getPendingPaymentCount()));
        metrics.put("pendingShipmentCount", safeLong(statistics.getPendingShipmentCount()));
        metrics.put("cancelledCount", safeLong(statistics.getCancelledCount()));
        metrics.put("cancelledRate", totalOrders > 0 ? ratio(safeLong(statistics.getCancelledCount()), totalOrders) : BigDecimal.ZERO);
        return metrics;
    }

    private Map<String, Object> buildDailyMetrics(OrderStatisticsResponse statistics, RestockAnalysisResponse restockAnalysis) {
        Map<String, Object> metrics = new LinkedHashMap<>();
        metrics.put("totalOrders", safeLong(statistics.getTotalOrders()));
        metrics.put("totalSales", statistics.getTotalSales());
        metrics.put("completedCount", safeLong(statistics.getCompletedCount()));
        metrics.put("highRiskRestockCount", safeInt(restockAnalysis.getHighRiskCount()));
        metrics.put("mediumRiskRestockCount", safeInt(restockAnalysis.getMediumRiskCount()));
        return metrics;
    }

    private RestockAnalysisResponse.RestockSuggestion findUrgentRestock(RestockAnalysisResponse restockAnalysis) {
        if (restockAnalysis.getSuggestions() == null || restockAnalysis.getSuggestions().isEmpty()) {
            return null;
        }
        for (RestockAnalysisResponse.RestockSuggestion suggestion : restockAnalysis.getSuggestions()) {
            if ("HIGH".equals(suggestion.getRiskLevel())) {
                return suggestion;
            }
        }
        for (RestockAnalysisResponse.RestockSuggestion suggestion : restockAnalysis.getSuggestions()) {
            if ("MEDIUM".equals(suggestion.getRiskLevel())) {
                return suggestion;
            }
        }
        return restockAnalysis.getSuggestions().get(0);
    }

    private BigDecimal ratio(long numerator, long denominator) {
        if (denominator <= 0) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(numerator)
                .divide(BigDecimal.valueOf(denominator), 4, BigDecimal.ROUND_HALF_UP);
    }

    private String formatPercent(BigDecimal ratio) {
        return ratio.multiply(BigDecimal.valueOf(100)).setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "%";
    }

    private String formatAmount(BigDecimal amount) {
        return amount == null ? "0.00" : amount.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString();
    }

    private int safeInt(Integer value) {
        return value == null ? 0 : value;
    }

    private long safeLong(Long value) {
        return value == null ? 0L : value;
    }

    /**
     * 构建 AI 上下文 JSON
     */
    private String buildContext(OrderStatisticsResponse statistics, RestockAnalysisResponse restockAnalysis, String questionType) {
        try {
            Map<String, Object> context = new LinkedHashMap<>();
            context.put("questionType", questionType);
            context.put("totalOrders", safeLong(statistics.getTotalOrders()));
            context.put("totalSales", statistics.getTotalSales());
            context.put("pendingPaymentCount", safeLong(statistics.getPendingPaymentCount()));
            context.put("pendingShipmentCount", safeLong(statistics.getPendingShipmentCount()));
            context.put("cancelledCount", safeLong(statistics.getCancelledCount()));
            context.put("completedCount", safeLong(statistics.getCompletedCount()));

            if (statistics.getTopProducts() != null) {
                List<Map<String, Object>> topProducts = new ArrayList<>();
                for (OrderStatisticsResponse.ProductStatistic product : statistics.getTopProducts()) {
                    Map<String, Object> productMap = new LinkedHashMap<>();
                    productMap.put("productName", product.getProductName());
                    productMap.put("salesQuantity", safeInt(product.getSalesQuantity()));
                    productMap.put("salesAmount", product.getSalesAmount());
                    topProducts.add(productMap);
                }
                context.put("topProducts", topProducts);
            }

            Map<String, Integer> restockAnalysisMap = new LinkedHashMap<>();
            restockAnalysisMap.put("highRiskCount", safeInt(restockAnalysis.getHighRiskCount()));
            restockAnalysisMap.put("mediumRiskCount", safeInt(restockAnalysis.getMediumRiskCount()));
            restockAnalysisMap.put("lowRiskCount", safeInt(restockAnalysis.getLowRiskCount()));
            context.put("restockAnalysis", restockAnalysisMap);

            if (restockAnalysis.getSuggestions() != null) {
                List<Map<String, Object>> suggestions = new ArrayList<>();
                for (RestockAnalysisResponse.RestockSuggestion suggestion : restockAnalysis.getSuggestions()) {
                    if (!"HIGH".equals(suggestion.getRiskLevel()) && !"MEDIUM".equals(suggestion.getRiskLevel())) {
                        continue;
                    }
                    Map<String, Object> suggestionMap = new LinkedHashMap<>();
                    suggestionMap.put("productName", suggestion.getProductName());
                    suggestionMap.put("currentStock", safeInt(suggestion.getCurrentStock()));
                    suggestionMap.put("salesQuantity", safeInt(suggestion.getSalesQuantity()));
                    suggestionMap.put("riskLevel", suggestion.getRiskLevel());
                    suggestionMap.put("suggestedRestockQuantity", safeInt(suggestion.getSuggestedRestockQuantity()));
                    suggestions.add(suggestionMap);
                }
                context.put("restockSuggestions", suggestions);
            }

            return objectMapper.writeValueAsString(context);
        } catch (JsonProcessingException e) {
            log.error("构建 AI 上下文失败", e);
            return "{}";
        }
    }

    /**
     * 生成 AI 摘要
     */
    private String generateAiSummary(String context, String questionType) {
        try {
            String summary = aiClientService.generateSummary(context, questionType);
            return summary != null ? summary : "";
        } catch (Exception e) {
            log.error("生成 AI 摘要失败", e);
            return "";
        }
    }

    /**
     * 生成 AI 建议详情
     */
    private String generateAiSuggestionDetail(String context, String questionType, String title, String level) {
        try {
            String enhancedContext = context + String.format("\n建议标题: %s\n建议等级: %s", title, level);
            String detail = aiClientService.generateSuggestion(enhancedContext, questionType);
            return detail != null ? detail : "";
        } catch (Exception e) {
            log.error("生成 AI 建议详情失败", e);
            return "";
        }
    }
}
