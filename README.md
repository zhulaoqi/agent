# AI Agent 智能助手系统

## 项目简介

这是一个基于 **Spring AI Alibaba** 框架构建的生产级AI助手系统，旨在解决当前AI应用开发中的核心痛点：

### 解决的核心问题

1. **审计缺失** ✅ - 通过 `AuditLogHook` 记录所有AI操作
2. **安全缺失** ✅ - 通过 `SecurityHook` 进行内容安全检查
3. **成本不可控** ✅ - 通过 `TokenLimitHook` 实现Token管理和成本控制
4. **任务规划不灵活** ✅ - 通过 `Graph Core` 实现工作流编排
5. **缺少人工控制** ✅ - 通过 `HumanInterventionHook` 实现人工介入
6. **上下文过长** ✅ - 通过 `Memory` 管理实现智能记忆

## 核心特性

### 1. ReactAgent 智能体
- 基于Spring AI Alibaba的ReactAgent
- 支持工具调用和推理循环
- 结构化输出
- 对话记忆持久化

### 2. Hooks 系统（核心创新）

| Hook类型 | 功能 | 解决的问题 |
|---------|------|----------|
| `AuditLogHook` | 完整审计日志 | 审计缺失 |
| `TokenLimitHook` | Token监控和限制 | 成本不可控 |
| `SecurityHook` | 内容安全检查 | 安全缺失 |
| `HumanInterventionHook` | 关键节点人工确认 | 任务规划不灵活 |
| `ModelCallLimitHook` | 防止无限循环 | 资源浪费 |

### 3. 丰富的Tools工具

- **WeatherTool** - 天气查询
- **NamingTool** - 编程命名助手（为技术人员服务）
- **ScriptGenerationTool** - 运维脚本生成（为运维人员服务）
- **ImageGenerationTool** - 文生图（计划中）
- **PptGenerationTool** - PPT生成（计划中）

### 4. 完整的业务功能

- 用户认证和授权（JWT + Spring Security）
- 会话管理（支持多轮对话）
- Token使用统计和成本分析
- 审计日志查询
- 生成内容管理
- 工作流执行追踪

## 技术栈

### 后端
- **Spring Boot 3.5.9**
- **Spring AI Alibaba 1.1.0.0-RC2** 
  - Agent Framework
  - Graph Core
  - Tools
  - Hooks
- **阿里云百炼（DashScope）**
- **Spring Security + JWT**
- **Spring Data JPA + H2**
- **Redis**（CheckPoint持久化）

### 前端（计划）
- **Vue 3**
- **TypeScript**
- **Element Plus**
- **Pinia**
- **Axios**

## 快速开始

### 1. 环境要求

- JDK 17+
- Maven 3.8+
- Redis（可选，用于持久化）
- 阿里云百炼 API Key

### 2. 配置API Key

设置环境变量：

```bash
export AI_DASHSCOPE_API_KEY=your-api-key-here
export JWT_SECRET=your-jwt-secret-here
```

或在 `application.properties` 中配置（不推荐）。

获取 API Key：访问 [阿里云百炼控制台](https://bailian.console.aliyun.com/)

### 3. 启动项目

```bash
# 编译项目
mvn clean package

# 运行
mvn spring-boot:run
```

访问：
- 应用: http://localhost:8080
- H2控制台: http://localhost:8080/h2-console

### 4. 默认用户

首次启动会自动创建管理员用户：
- 用户名: `admin`
- 密码: `admin123`

## 项目结构

```
agent/
├── src/main/java/com/kinch/agent/
│   ├── entity/          # 数据库实体
│   │   ├── User.java
│   │   ├── Conversation.java
│   │   ├── Message.java
│   │   ├── AuditLog.java        # 审计日志
│   │   ├── TokenUsageLog.java   # Token使用记录
│   │   └── WorkflowExecution.java
│   ├── repository/      # 数据访问层
│   ├── hook/            # Hooks实现（核心创新）
│   │   ├── AuditLogHook.java      # 审计
│   │   ├── TokenLimitHook.java    # Token控制
│   │   ├── SecurityHook.java      # 安全检查
│   │   └── HumanInterventionHook.java  # 人工介入
│   ├── tool/            # Tools工具
│   │   ├── WeatherTool.java
│   │   ├── NamingTool.java
│   │   └── ScriptGenerationTool.java
│   ├── service/         # 业务服务层
│   ├── controller/      # REST API
│   ├── config/          # 配置类
│   │   ├── AgentConfig.java       # Agent配置
│   │   └── SecurityConfig.java    # 安全配置
│   └── dto/             # 数据传输对象
└── src/main/resources/
    └── application.properties
```

## API 文档

### 认证接口

#### 注册
```http
POST /api/auth/register
Content-Type: application/json

{
  "username": "testuser",
  "password": "password123",
  "nickname": "测试用户",
  "email": "test@example.com"
}
```

#### 登录
```http
POST /api/auth/login
Content-Type: application/json

{
  "username": "testuser",
  "password": "password123"
}
```

### Agent对话接口

```http
POST /api/agent/chat
Authorization: Bearer {token}
Content-Type: application/json

{
  "message": "帮我查询北京的天气",
  "conversationId": 1  // 可选
}
```

### 工具调用示例

#### 天气查询
```
用户：北京今天天气怎么样？
Agent：[调用 getWeather 工具] → 返回天气信息
```

#### 命名助手
```
用户：帮我生成一个用户服务类的Java类名
Agent：[调用 generateNaming 工具] → 返回命名建议
```

#### 脚本生成
```
用户：帮我生成一个备份日志文件的Shell脚本
Agent：[调用 generateScript 工具] → 生成并保存脚本
```

## 核心设计理念

### 1. 可观测性（Observability）

通过 `AuditLogHook`，系统记录：
- 每次AI调用
- 每次工具调用
- 输入输出内容
- 执行时间
- Token消耗
- 错误信息

### 2. 成本治理（Cost Governance）

通过 `TokenLimitHook`，系统实现：
- 每日Token限额
- 实时Token监控
- 成本预估
- 超限报警
- 用户级别配额管理

### 3. 安全合规（Security & Compliance）

通过 `SecurityHook`，系统保障：
- 敏感词过滤
- 危险操作拦截
- 内容脱敏
- 输入验证

### 4. 灵活编排（Flexible Orchestration）

通过 `Graph Core`（计划中），系统支持：
- 工作流可视化
- 动态任务规划
- 条件分支
- 并行执行
- 人工审批节点

## 数据库设计

### 核心表

- `t_user` - 用户信息和Token配额
- `t_conversation` - 会话记录（含threadId）
- `t_message` - 消息历史
- `t_audit_log` - **审计日志**（核心）
- `t_token_usage_log` - **Token使用记录**（核心）
- `t_workflow_execution` - 工作流执行记录
- `t_generated_content` - 生成内容（图片、PPT、脚本等）

## 性能优化

1. **Token优化**
   - 限制历史消息数量（最近10条）
   - 使用结构化输出减少Token
   - 智能摘要长对话

2. **缓存策略**
   - Redis缓存CheckPoint
   - 对话记忆持久化

3. **异步处理**
   - 审计日志异步写入
   - 文件生成异步处理

## 扩展开发

### 添加新Tool

```java
@Component
public class MyCustomTool implements BiFunction<String, ToolContext, String> {
    @Override
    public String apply(
            @ToolParam(description = "参数描述") String input,
            ToolContext toolContext) {
        // 实现逻辑
        return "result";
    }
}
```

### 添加新Hook

```java
@Component
public class MyCustomHook implements Hook {
    @Override
    public void onAgentStart(String agentName, OverAllState state, RunnableConfig config) {
        // 实现逻辑
    }
}
```

## 部署建议

### 生产环境配置

1. **使用真实数据库**（PostgreSQL/MySQL）
2. **Redis集群**用于CheckPoint
3. **配置日志收集**（ELK/Loki）
4. **监控告警**（Prometheus + Grafana）
5. **API网关**进行限流和鉴权

### 容器化部署

```dockerfile
FROM openjdk:17-slim
COPY target/agent-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

## 参考文档

- [Spring AI Alibaba官方文档](https://java2ai.com/docs/quick-start)
- [阿里云百炼文档](https://help.aliyun.com/zh/dashscope/)
- [Spring Security文档](https://spring.io/projects/spring-security)

## License

MIT License

## 作者

kinch - 2025

---

**核心优势：这不仅是一个AI应用，更是一套完整的AI应用治理方案！**


