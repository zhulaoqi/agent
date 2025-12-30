# Spring AI Alibaba Agent 框架实践项目

> 基于 **Spring AI Alibaba** 构建的生产级AI Agent系统，深度集成框架核心能力

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.1-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Spring AI Alibaba](https://img.shields.io/badge/Spring%20AI%20Alibaba-1.0.0--M2.3-blue.svg)](https://spring-ai-alibaba.github.io/)
[![Vue](https://img.shields.io/badge/Vue-3.4-green.svg)](https://vuejs.org/)
[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)

## 📖 项目简介

这是一个**深度实践Spring AI Alibaba框架**的完整项目，旨在展示该框架的核心能力，并提供生产级的实现参考。

### 🎯 核心目标

- ✅ **完整展示框架能力**：ReactAgent、Hooks、Interceptors、StateGraph、Multi-Agent、RAG等
- ✅ **生产级实现**：包含监控、审计、安全、性能优化等企业级特性
- ✅ **开箱即用**：提供完整的前后端实现，可直接运行和二次开发
- ✅ **最佳实践**：展示框架的正确使用方式和设计模式

---

## 🚀 核心功能

### 1. ReactAgent 智能体

**基于Spring AI Alibaba的核心Agent实现**

- 🤖 **智能对话**：支持多轮对话、上下文理解
- 🛠️ **工具调用**：自动识别意图并调用相应工具
- 💾 **记忆管理**：会话持久化、历史记录
- 📊 **结构化输出**：返回格式化的数据结构

**实现文件**：
- `AgentService.java` - Agent核心服务
- `AgentConfig.java` - Agent配置和工厂

---

### 2. Hooks & Interceptors（核心创新）

**在Agent执行流程的关键点插入自定义逻辑**

| 类型 | 实现类 | 功能 | 应用场景 |
|------|--------|------|---------|
| **ModelHook** | `CustomAuditHook` | 审计日志记录 | 记录所有模型调用、耗时、Token消耗 |
| **ModelHook** | `CustomSecurityHook` | 安全检查 | SQL注入、命令注入、敏感词检测 |
| **ModelHook** | `CustomTokenLimitHook` | Token配额管理 | 用户级别Token限制、自动扣费 |
| **MessagesModelHook** | `MessageTrimmingHook` | 消息修剪 | 控制上下文窗口大小，降低成本 |
| **ModelInterceptor** | `PerformanceInterceptor` | 性能监控 | 监控模型调用耗时、性能分析 |
| **ModelInterceptor** | `SafetyInterceptor` | 内容安全 | 敏感内容过滤、输出清洗 |
| **ToolInterceptor** | `ToolMonitorInterceptor` | 工具监控 | 监控工具调用成功率、耗时 |

**实现目录**：
- `src/main/java/com/kinch/agent/hook/` - Hooks实现
- `src/main/java/com/kinch/agent/interceptor/` - Interceptors实现

---

### 3. StateGraph 工作流编排

**真实的图状态工作流引擎（非简化版）**

**支持的工作流类型**：

#### 开发工作流（Development Workflow）
```
ReadRequirement → ClassifyRequirement → [Quick/Detailed]Analysis 
  → GenerateSolution → GenerateCode → GenerateTests → HumanReview → END
```

#### 条件分支工作流（Conditional Workflow）
```
ClassifyContent → [Technical/Business/General]Handler → END
```

**核心特性**：
- ✅ **节点（Nodes）**：每个节点是一个处理单元
- ✅ **边（Edges）**：节点间的连接
- ✅ **条件边（Conditional Edges）**：动态路由
- ✅ **状态管理（State）**：跨节点共享数据
- ✅ **编译执行（Compiled Graph）**：优化的执行引擎

**实现文件**：
- `RealStateGraphWorkflow.java` - StateGraph核心实现

---

### 4. 丰富的工具集（Tools）

| 工具 | 类名 | 功能描述 | 实际应用 |
|------|------|---------|---------|
| 🌤️ **天气查询** | `WeatherTool` | 查询城市天气信息 | 支持高德地图API + 模拟数据 |
| 🏷️ **命名助手** | `NamingTool` | 中文转编程命名 | 驼峰、下划线、帕斯卡命名 |
| 📝 **脚本生成** | `ScriptGenerationTool` | 生成运维脚本 | Bash、Python、PowerShell |
| 🗄️ **数据库查询** | `DatabaseQueryTool` | SQL查询执行 | 支持安全查询和结果格式化 |
| 🌐 **API调用** | `ApiCallTool` | HTTP请求工具 | GET/POST请求，JSON解析 |
| 📁 **文件操作** | `FileOperationTool` | 文件读写 | 读取、写入、列表文件 |
| 🎨 **图片生成** | `ImageGenerationTool` | AI文生图 | 通义万相（Wanx-v1）|

**实现目录**：
- `src/main/java/com/kinch/agent/tool/` - 所有工具实现

---

### 5. RAG 知识库问答

**检索增强生成（Retrieval Augmented Generation）**

- 📚 **知识库管理**：文档上传、向量化、存储
- 🔍 **语义检索**：基于Embedding的相似度搜索
- 🧠 **上下文增强**：结合检索内容生成回答
- 📊 **来源追溯**：标注答案来源文档

**实现文件**：
- `RagService.java` - RAG核心服务
- `RagController.java` - RAG API接口

---

### 6. Multi-Agent 多智能体

**多个Agent协作完成复杂任务**

**支持模式**：
- 👨‍💼 **监督者模式（Supervisor）**：一个主Agent协调多个子Agent
- ⚡ **并行执行（Parallel）**：多个Agent并发处理任务
- 💬 **Agent通信**：Agent间消息传递和结果共享

**应用场景**：
- 复杂文档分析（多角度分析）
- 代码审查（多个审查员）
- 数据处理流水线

**实现文件**：
- `MultiAgentService.java` - Multi-Agent服务
- `MultiAgentController.java` - Multi-Agent API

---

### 7. Human-in-the-Loop 人机协同

**关键决策点引入人工审批**

**支持功能**：
- ✋ **暂停执行**：在指定节点暂停
- ✅ **人工审批**：同意/拒绝/修改
- 🔄 **断点续传**：从中断点继续执行
- 📝 **审批记录**：完整的审批日志

**应用场景**：
- 高风险操作（数据库删除、发送邮件）
- 合规要求（金融审批、医疗决策）
- 质量把控（内容审核、代码发布）

**实现文件**：
- `HumanInLoopService.java` - 人机协同服务
- `HumanInLoopController.java` - 人机协同API

---

### 8. 监控与审计

**完整的监控审计体系**

**监控指标**：
- 📊 **模型调用统计**：总次数、总耗时、平均耗时
- 🛠️ **工具调用统计**：成功/失败次数、耗时分析
- 🔒 **安全检查统计**：通过/拒绝次数
- 💰 **成本统计**：Token消耗、费用估算

**审计日志**：
- 🕐 **时间戳**：精确到毫秒
- 👤 **用户信息**：userId、username、IP地址
- 📝 **操作详情**：输入、输出、工具调用
- ⏱️ **性能数据**：执行时间、Token消耗

**实现文件**：
- `MonitorService.java` - 监控数据服务
- `MonitorController.java` - 监控API接口
- `CustomAuditHook.java` - 审计Hook

---

### 9. 提示词管理

**企业级Prompt Template管理**

**核心功能**：
- 💾 **持久化存储**：MySQL数据库存储
- ⚡ **Redis缓存**：高性能读取
- 🔄 **版本管理**：支持多版本共存
- 🏷️ **分类标签**：按场景分类管理
- 🔍 **模糊搜索**：支持标题、标签搜索

**变量替换**：
```java
String prompt = "你好，{name}！今天是{date}";
Map<String, String> vars = Map.of("name", "张三", "date", "2025-12-30");
// 结果: "你好，张三！今天是2025-12-30"
```

**实现文件**：
- `PromptManagementServiceV2.java` - 提示词管理服务
- `PromptController.java` - 提示词管理API
- `PromptTemplate.java` - 提示词实体

---

### 10. 流式响应（SSE）

**服务端推送事件（Server-Sent Events）**

- ⚡ **实时响应**：逐字输出，提升用户体验
- 🔄 **流式工具调用**：支持工具调用的流式输出
- 📊 **流式分析**：文本分析、代码生成的流式展示

**实现文件**：
- `StreamingService.java` - 流式服务
- `StreamingController.java` - 流式API

---

## 🏗️ 技术架构

### 后端技术栈

```
┌─────────────────────────────────────────────────┐
│          Spring AI Alibaba 框架层                │
├─────────────────────────────────────────────────┤
│  ReactAgent │ Hooks │ Interceptors │ StateGraph │
├─────────────────────────────────────────────────┤
│               业务服务层 (Service)                │
│  AgentService │ RagService │ MultiAgentService  │
├─────────────────────────────────────────────────┤
│               REST API层 (Controller)            │
│  AgentController │ ToolController │ MonitorAPI  │
├─────────────────────────────────────────────────┤
│        数据持久层 (Repository + JPA)             │
│  MySQL │ Redis │ Conversation │ AuditLog        │
└─────────────────────────────────────────────────┘
```

**核心依赖**：
- Spring Boot 3.2.1
- Spring AI Alibaba 1.0.0-M2.3
- 通义千问（qwen-plus、qwen-max）
- 通义万相（wanx-v1）
- MySQL 8.0+
- Redis 7.0+
- Spring Data JPA

### 前端技术栈

```
┌─────────────────────────────────────────────────┐
│                Vue 3 Composition API             │
├─────────────────────────────────────────────────┤
│          Element Plus UI 组件库                  │
├─────────────────────────────────────────────────┤
│         Axios HTTP客户端 + SSE支持               │
├─────────────────────────────────────────────────┤
│       Vue Router + 路由守卫（认证）              │
└─────────────────────────────────────────────────┘
```

**核心依赖**：
- Vue 3.4+
- Element Plus 2.5+
- Axios 1.6+
- Vue Router 4.2+

---

## 📦 项目结构

```
agent/
├── src/main/java/com/kinch/agent/
│   ├── config/                    # 配置类
│   │   ├── AgentConfig.java       # Agent核心配置（重要）
│   │   ├── SecurityConfig.java    # 安全配置
│   │   └── WebConfig.java         # Web配置
│   ├── hook/                      # Hooks实现（框架特性）
│   │   ├── CustomAuditHook.java           # 审计Hook
│   │   ├── CustomSecurityHook.java        # 安全Hook
│   │   ├── CustomTokenLimitHook.java      # Token限制Hook
│   │   └── MessageTrimmingHook.java       # 消息修剪Hook
│   ├── interceptor/               # Interceptors实现（框架特性）
│   │   ├── PerformanceInterceptor.java    # 性能监控
│   │   ├── SafetyInterceptor.java         # 内容安全
│   │   └── ToolMonitorInterceptor.java    # 工具监控
│   ├── tool/                      # 工具集
│   │   ├── WeatherTool.java       # 天气查询
│   │   ├── NamingTool.java        # 命名助手
│   │   ├── ScriptGenerationTool.java  # 脚本生成
│   │   ├── DatabaseQueryTool.java     # 数据库查询
│   │   ├── ApiCallTool.java           # API调用
│   │   ├── FileOperationTool.java     # 文件操作
│   │   └── ImageGenerationTool.java   # 图片生成
│   ├── workflow/                  # 工作流编排
│   │   └── RealStateGraphWorkflow.java  # StateGraph实现
│   ├── service/                   # 业务服务
│   │   ├── AgentService.java      # Agent核心服务
│   │   ├── StreamingService.java  # 流式服务
│   │   ├── RagService.java        # RAG服务
│   │   ├── MultiAgentService.java # Multi-Agent服务
│   │   ├── HumanInLoopService.java    # 人机协同服务
│   │   ├── MonitorService.java        # 监控服务
│   │   ├── PromptManagementServiceV2.java  # 提示词管理
│   │   └── ImageGenerationService.java     # 图片生成服务
│   ├── controller/                # REST API
│   │   ├── AgentController.java       # Agent API
│   │   ├── StreamingController.java   # 流式API
│   │   ├── ToolController.java        # 工具API
│   │   ├── WorkflowController.java    # 工作流API
│   │   ├── RagController.java         # RAG API
│   │   ├── MultiAgentController.java  # Multi-Agent API
│   │   ├── HumanInLoopController.java # 人机协同API
│   │   ├── MonitorController.java     # 监控API
│   │   ├── PromptController.java      # 提示词管理API
│   │   └── ImageController.java       # 图片生成API
│   ├── entity/                    # 数据库实体
│   │   ├── User.java              # 用户
│   │   ├── Conversation.java      # 会话
│   │   ├── Message.java           # 消息
│   │   ├── AuditLog.java          # 审计日志
│   │   ├── TokenUsageLog.java     # Token使用日志
│   │   └── PromptTemplate.java    # 提示词模板
│   └── repository/                # 数据访问层
├── src/main/resources/
│   ├── application.yml            # 应用配置
│   └── sql/
│       └── init.sql               # 数据库初始化脚本
└── frontend/                      # 前端项目
    ├── src/
    │   ├── views/                 # 页面组件
    │   │   ├── Chat.vue           # 对话页面
    │   │   ├── ChatSimple.vue     # 简化对话页面
    │   │   ├── Workflow.vue       # 工作流页面
    │   │   ├── ImageGen.vue       # 图片生成页面
    │   │   ├── RagDemo.vue        # RAG演示页面
    │   │   ├── MultiAgentDemo.vue # Multi-Agent演示
    │   │   ├── HumanInLoopDemo.vue    # 人机协同演示
    │   │   └── FrameworkCenter.vue    # 框架中心（总览）
    │   ├── api/
    │   │   └── agent.js           # API封装
    │   └── router/
    │       └── index.js           # 路由配置
    └── package.json
```

---

## 🚀 快速开始

### 环境要求

- ☕ **JDK 17+**
- 📦 **Maven 3.8+**
- 🐬 **MySQL 8.0+**
- 🔴 **Redis 7.0+**
- 🟢 **Node.js 18+**（前端）
- 🔑 **阿里云API Key**（通义千问 + 通义万相）

### 1. 获取API Key

访问 [阿里云百炼控制台](https://bailian.console.aliyun.com/)：
1. 注册/登录阿里云账号
2. 开通 **通义千问** 和 **通义万相** 服务
3. 创建API Key
4. 充值（建议100元起）

### 2. 配置数据库

**创建数据库**：
```sql
CREATE DATABASE agent_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

**执行初始化脚本**：
```bash
mysql -u root -p agent_db < src/main/resources/sql/init.sql
```

### 3. 配置应用

编辑 `src/main/resources/application.yml`：

```yaml
spring:
  ai:
    dashscope:
      api-key: sk-xxxxxxxxxxxxxxxx  # 你的API Key
  
  datasource:
    url: jdbc:mysql://localhost:3306/agent_db
    username: root
    password: your_password
  
  data:
    redis:
      host: localhost
      port: 6379
```

**或使用环境变量（推荐）**：
```bash
export DASHSCOPE_API_KEY=sk-xxxxxxxxxxxxxxxx
export MYSQL_PASSWORD=your_password
```

### 4. 启动后端

```bash
# 编译项目
mvn clean package -DskipTests

# 启动应用
mvn spring-boot:run

# 或直接运行JAR
java -jar target/agent-0.0.1-SNAPSHOT.jar
```

**启动成功标志**：
```
🚀 Agent应用启动成功！
📍 地址: http://localhost:8080
📖 API文档: http://localhost:8080/swagger-ui.html
```

### 5. 启动前端

```bash
cd frontend

# 安装依赖
npm install

# 启动开发服务器
npm run dev

# 访问: http://localhost:5173
```

### 6. 测试接口

**健康检查**：
```bash
curl http://localhost:8080/api/test/health
```

**对话测试**：
```bash
curl -X POST http://localhost:8080/api/agent/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "你好"}'
```

---

## 📚 使用示例

### 1. 对话示例

```bash
用户：北京今天天气怎么样？
Agent：正在查询北京天气... 
      [调用 WeatherTool]
      北京今天晴，温度15°C，湿度45%，空气质量良好
```

### 2. 工具调用示例

```bash
用户：帮我生成一个用户服务的类名
Agent：[调用 NamingTool]
      建议的命名：
      - 驼峰：userService
      - 帕斯卡：UserService
      - 下划线：user_service
```

### 3. 工作流示例

```bash
用户：帮我开发一个用户注册功能
Agent：[执行 Development Workflow]
      1. 理解需求 ✓
      2. 需求分类（详细分析）✓
      3. 生成方案 ✓
      4. 生成代码 ✓
      5. 生成测试 ✓
      6. 等待人工审核...
```

### 4. RAG示例

```bash
用户：根据我的知识库回答：公司的年假政策是什么？
Agent：[检索知识库]
      [找到相关文档：员工手册.pdf]
      根据公司政策，正式员工享有10天年假...
      来源：员工手册.pdf (第23页)
```

---

## 🔧 配置说明

### Agent配置

在 `AgentConfig.java` 中配置：

```java
@Bean
public ReactAgent.Builder agentBuilder() {
    return ReactAgent.builder()
        .name("my_agent")
        .model(chatModel)
        .tools(weatherTool, namingTool)  // 配置工具
        .hooks(auditHook, securityHook)  // 配置Hooks
        .interceptors(performanceInterceptor)  // 配置Interceptors
        .maxIterations(10);  // 最大迭代次数
}
```

### Hooks配置

**启用/禁用Hooks**：
```java
// 在AgentConfig中选择性添加
.hooks(
    customAuditHook,           // 审计日志（建议保留）
    customSecurityHook,        // 安全检查（建议保留）
    customTokenLimitHook,      // Token限制（按需启用）
    messageTrimmingHook        // 消息修剪（按需启用）
)
```

### StateGraph配置

```java
// 自定义节点
StateGraph<OverAllState> graph = StateGraph.builder()
    .addNode("node1", new CustomNodeAction())
    .addNode("node2", new AnotherNodeAction())
    .addEdge("node1", "node2")  // 顺序边
    .addConditionalEdge("node2", this::routeLogic)  // 条件边
    .compile();
```

---

## 📊 监控与审计

### 查看监控数据

**API接口**：
```bash
GET /api/monitor/stats
```

**返回示例**：
```json
{
  "totalModelCalls": 1523,
  "totalModelTimeMs": 45678,
  "avgModelTimeMs": 30.0,
  "totalToolCalls": 856,
  "successfulToolCalls": 831,
  "failedToolCalls": 25,
  "totalSecurityChecks": 1523,
  "passedSecurityChecks": 1520,
  "failedSecurityChecks": 3
}
```

### 查看审计日志

**数据库查询**：
```sql
SELECT * FROM t_audit_log 
WHERE user_id = 1 
ORDER BY create_time DESC 
LIMIT 10;
```

---

## 🎯 生产部署

### Docker部署

**Dockerfile**：
```dockerfile
FROM openjdk:17-slim
WORKDIR /app
COPY target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

**docker-compose.yml**：
```yaml
version: '3.8'
services:
  agent-app:
    build: .
    ports:
      - "8080:8080"
    environment:
      - DASHSCOPE_API_KEY=${DASHSCOPE_API_KEY}
      - MYSQL_HOST=mysql
      - REDIS_HOST=redis
    depends_on:
      - mysql
      - redis
  
  mysql:
    image: mysql:8.0
    environment:
      MYSQL_DATABASE: agent_db
      MYSQL_ROOT_PASSWORD: ${MYSQL_PASSWORD}
  
  redis:
    image: redis:7-alpine
```

### Kubernetes部署

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: agent-app
spec:
  replicas: 3
  template:
    spec:
      containers:
      - name: agent
        image: your-registry/agent:latest
        env:
        - name: DASHSCOPE_API_KEY
          valueFrom:
            secretKeyRef:
              name: agent-secrets
              key: api-key
```

---

## 🤝 贡献指南

欢迎贡献！请遵循以下步骤：

1. Fork本仓库
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 创建Pull Request

---

## 📄 许可证

本项目采用 MIT 许可证 - 详见 [LICENSE](LICENSE) 文件

---

## 🙏 致谢

- [Spring AI Alibaba](https://github.com/alibaba/spring-ai-alibaba) - 核心框架
- [阿里云百炼](https://www.aliyun.com/product/bailian) - AI模型支持
- [Spring Boot](https://spring.io/projects/spring-boot) - 应用框架
- [Vue.js](https://vuejs.org/) - 前端框架

---

## 📮 联系方式

- 作者：kinch
- 邮箱：your-email@example.com
- 问题反馈：[GitHub Issues](https://github.com/your-username/agent/issues)

---

## 🎓 学习资源

- [Spring AI Alibaba官方文档](https://spring-ai-alibaba.github.io/)
- [阿里云百炼文档](https://help.aliyun.com/zh/dashscope/)
- [项目实战教程](./docs/tutorial.md)（待补充）

---

**⭐ 如果这个项目对你有帮助，请给个Star！**

---

## 📝 更新日志

### v1.0.0 (2025-12-30)

- ✅ 完整实现Spring AI Alibaba核心功能
- ✅ 集成ReactAgent + Hooks + Interceptors
- ✅ 实现StateGraph真实工作流编排
- ✅ 添加RAG、Multi-Agent、Human-in-the-Loop
- ✅ 完善监控审计体系
- ✅ 实现提示词管理（MySQL + Redis）
- ✅ 完整的前端界面（Vue3）
- ✅ 生产级配置和部署方案

---

**这不仅是一个Demo，而是一个可直接用于生产的完整解决方案！** 🚀
