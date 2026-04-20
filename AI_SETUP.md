# AI 功能配置指南

## 概述

本项目已集成 DeepSeek AI 功能，通过 OpenAI 兼容 API 提供智能运营建议。

## 配置步骤

### 1. 环境变量（推荐）

创建 `.env` 文件或在系统环境变量中设置：

```bash
# DeepSeek API 配置
AI_API_KEY=sk-a1c78d1b3b544d5d961582cf52249cc7
AI_MODEL=deepseek-chat
AI_BASE_URL=https://api.deepseek.com

# 可选：禁用 AI 功能
# AI_ENABLED=false
```

或者在 `application.yml` 中直接配置（不推荐用于生产环境）。

### 2. 配置文件

AI 配置位于 `backend/src/main/resources/application.yml`：

```yaml
ai:
  api-key: ${AI_API_KEY:sk-a1c78d1b3b544d5d961582cf52249cc7}
  model: ${AI_MODEL:deepseek-chat}
  base-url: ${AI_BASE_URL:https://api.deepseek.com}
  temperature: 0.7
  max-tokens: 2000
  enabled: true
```

### 3. 依赖说明

已添加 OpenAI Java 客户端依赖（兼容 DeepSeek API）：

```xml
<dependency>
    <groupId>com.theokanning.openai-gpt3-java</groupId>
    <artifactId>service</artifactId>
    <version>0.18.2</version>
</dependency>
```

## 功能特性

### AI 增强的运营建议

1. **热销商品分析** - 智能识别热销商品并提供库存和营销建议
2. **补货建议** - 基于库存和销售数据的智能补货推荐
3. **订单预警** - 识别订单结构异常并提供处理建议
4. **每日摘要** - 综合运营数据分析报告

### 回退机制

- 当 AI 服务不可用（API 密钥未配置、网络错误等）时，自动使用原有规则引擎
- 确保系统在 AI 服务异常时仍能正常工作

## API 接口

### 智能问讯接口

```
GET /api/admin/ai-assistant/ask
```

参数：
- `questionType`: 问题类型（HOT_PRODUCTS, RESTOCK_SUGGESTION, ORDER_ALERT, DAILY_SUMMARY）
- `rangeType`: 时间范围（7d, 15d, 30d）
- `startDate`: 自定义开始日期（可选）
- `endDate`: 自定义结束日期（可选）

## 开发说明

### 代码结构

```
backend/src/main/java/com/shop/
├── config/
│   ├── AiProperties.java      # AI 配置属性类
│   └── AiConfig.java          # AI 客户端配置
├── service/
│   ├── AiClientService.java   # AI 客户端接口
│   └── AiAssistantService.java
└── service/impl/
    ├── AiClientServiceImpl.java    # AI 客户端实现
    └── AiAssistantServiceImpl.java # AI 问讯服务实现（已集成 AI）
```

### 扩展 AI 功能

1. **添加新的问题类型**：
   - 在 `AiAssistantServiceImpl` 中添加新的处理逻辑
   - 更新 `AiAssistantQueryRequest` 和前端类型定义

2. **自定义提示词**：
   - 修改 `AiClientServiceImpl.buildSuggestionPrompt()` 和 `buildSummaryPrompt()`
   - 调整提示词模板以优化 AI 响应质量

3. **调整 AI 参数**：
   - 修改 `application.yml` 中的 `temperature` 和 `max-tokens`
   - 温度值范围 0.0-1.0（越高越随机）

## 故障排除

### 常见问题

1. **AI 功能未生效**
   - 检查 API 密钥是否正确
   - 确认网络可以访问 `https://api.deepseek.com`
   - 查看应用日志中的 AI 客户端初始化信息

2. **响应速度慢**
   - 检查网络连接
   - 调整 `max-tokens` 减少响应长度
   - 考虑增加超时时间配置

3. **AI 建议质量不高**
   - 调整提示词模板
   - 修改温度参数（降低温度使响应更确定）
   - 优化传递给 AI 的上下文数据

### 日志查看

AI 相关日志以 `com.shop.service.impl.AiClientServiceImpl` 和 `com.shop.config.AiConfig` 为前缀。

## 安全建议

1. **API 密钥安全**
   - 生产环境务必使用环境变量，不要在代码中硬编码
   - 定期轮换 API 密钥
   - 设置 API 使用限额

2. **数据隐私**
   - AI 服务供应商会收到发送的数据，请勿发送敏感信息
   - 考虑对数据进行脱敏处理

## 下一步优化建议

1. 实现异步 AI 调用，避免阻塞请求线程
2. 添加 AI 响应缓存，提高重复查询性能
3. 实现更细粒度的 AI 提示词定制
4. 添加 AI 使用统计和监控

---

*最后更新：2026-04-21*