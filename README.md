<div align="center">

# 🤖 Spring AI Alibaba Agent 框架

**基于 Spring AI Alibaba 构建的企业级智能体系统**

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.1-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Spring AI Alibaba](https://img.shields.io/badge/Spring%20AI%20Alibaba-1.0.0--M2.3-blue.svg)](https://github.com/alibaba/spring-ai-alibaba)
[![Vue](https://img.shields.io/badge/Vue-3.4-42b883.svg)](https://vuejs.org/)
[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)
[![PRs Welcome](https://img.shields.io/badge/PRs-welcome-brightgreen.svg)](CONTRIBUTING.md)

**[核心特性](#-核心特性) • [快速开始](#-快速开始) • [技术架构](#-技术架构) • [在线演示](#-在线演示)**

</div>

---

## 📖 项目简介

这是一个**生产级AI智能体系统**，深度展示了 Spring AI Alibaba 框架的完整能力。项目包含企业级实践模式：ReactAgent、Hooks & Interceptors、StateGraph工作流、Multi-Agent协作、RAG知识库等核心功能。

### 🎯 为什么需要这个项目？

> **当前AI应用开发的六大痛点与解决方案**

| 痛点 | 解决方案 | 技术实现 |
|------|---------|---------|
| **❌ 缺乏可观测性** | 完整的审计日志 | `CustomAuditHook` + `MonitorService` |
| **❌ 安全风险高** | 多层安全检查 | `CustomSecurityHook` + `SafetyInterceptor` |
| **❌ 成本不可控** | Token配额管理 | `CustomTokenLimitHook` |
| **❌ 工作流复杂** | 可视化编排引擎 | `StateGraph` 条件路由 |
| **❌ AI黑盒化** | 人工审批节点 | `HumanInLoopService` |
| **❌ 上下文溢出** | 智能消息修剪 | `MessageTrimmingHook` |

---

## ✨ 核心特性

### 🤖 智能体核心能力

<table>
<tr>
<td width="50%">

#### ReactAgent 反应式智能体
- 🧠 上下文理解的智能对话
- 🛠️ 自动工具选择与执行
- 💾 持久化记忆管理
- 📊 结构化输出格式
- ⚡ 流式响应（SSE）

</td>
<td width="50%">

#### 高级应用模式
- 📚 **RAG**: 知识库问答系统
- 👥 **Multi-Agent**: 监督者模式
- 🤝 **Human-in-Loop**: 审批工作流
- 🔀 **StateGraph**: 复杂编排
- 🎨 **AI绘画**: 文生图集成

</td>
</tr>
</table>

### 🎯 Hooks & Interceptors（框架深度实践）

我们的实现完整展示了 Spring AI Alibaba 的扩展能力：

```java
ReactAgent agent = ReactAgent.builder()
    .name("生产环境智能体")
    .model(chatModel)
    .tools(weatherTool, databaseTool, apiTool)
    .hooks(auditHook, securityHook, tokenLimitHook)
    .interceptors(performanceInterceptor, safetyInterceptor)
    .build();
```

<details>
<summary><b>📋 完整的Hooks & Interceptors清单</b></summary>

| 类型 | 实现类 | 功能说明 | 监控指标 |
|------|--------|----------|---------|
| **ModelHook** | `CustomAuditHook` | 审计日志记录 | 耗时、Token、输入输出 |
| **ModelHook** | `CustomSecurityHook` | 安全验证 | SQL注入、PII检测 |
| **ModelHook** | `CustomTokenLimitHook` | 配额管理 | 用户限额、成本 |
| **MessagesModelHook** | `MessageTrimmingHook` | 上下文优化 | 消息数量 |
| **ModelInterceptor** | `PerformanceInterceptor` | 性能监控 | 延迟、吞吐量 |
| **ModelInterceptor** | `SafetyInterceptor` | 内容过滤 | 不安全内容 |
| **ToolInterceptor** | `ToolMonitorInterceptor` | 工具分析 | 成功率、耗时 |

</details>

### 🛠️ 丰富的工具生态

| 工具 | 说明 | 使用场景 |
|------|------|---------|
| 🌤️ **WeatherTool** | 实时天气查询 | "北京今天天气怎么样？" |
| 🏷️ **NamingTool** | 代码命名助手 | "生成用户服务的Java类名" |
| 📝 **ScriptTool** | 运维脚本生成 | "创建日志备份的Bash脚本" |
| 🗄️ **DatabaseTool** | 安全SQL执行 | "查询前10名用户" |
| 🌐 **ApiTool** | HTTP请求处理 | "调用带认证的REST API" |
| 📁 **FileTool** | 文件操作 | "读取config.json" |
| 🎨 **ImageTool** | AI图片生成 | "创建日落风景画" |

### 🔀 StateGraph 工作流编排

真正的图状态工作流编排（非简化版）：

```
┌─────────────┐
│   需求分类   │
│ Classify    │
└──────┬──────┘
       │
   ┌───┴────┐
   │        │
   ▼        ▼
┌──────┐ ┌──────┐
│快速  │ │详细  │
│分析  │ │分析  │
└──┬───┘ └───┬──┘
   │         │
   └────┬────┘
        ▼
   ┌─────────┐
   │生成代码  │
   │  Code   │
   └─────────┘
```

**支持特性：**
- ✅ 动态节点路由
- ✅ 条件边判断
- ✅ 跨节点状态管理
- ✅ 人工审批节点
- ✅ 错误处理与重试

---

## 🚀 快速开始

### 环境要求

```bash
☕ Java 17+          🐬 MySQL 8.0+
📦 Maven 3.8+        🔴 Redis 7.0+
🟢 Node.js 18+       🔑 阿里云API密钥
```

### 一键安装（5分钟）

```bash
# 1. 克隆仓库
git clone https://github.com/zhulaoqi/agent.git
cd agent

# 2. 数据库初始化
mysql -u root -p -e "CREATE DATABASE agent_db CHARACTER SET utf8mb4;"
mysql -u root -p agent_db < src/main/resources/sql/init.sql

# 3. 配置API密钥
export DASHSCOPE_API_KEY=sk-你的密钥

# 4. 启动后端
mvn spring-boot:run

# 5. 启动前端（新终端）
cd frontend && npm install && npm run dev
```

**访问地址：**
- 🌐 前端界面: http://localhost:5173
- 🔌 后端服务: http://localhost:8080
- 📖 API文档: http://localhost:8080/swagger-ui.html

### 第一个请求

```bash
curl -X POST http://localhost:8080/api/agent/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "上海今天天气怎么样？"}'
```

---

## 🏗️ 技术架构

### 系统架构图

```
┌─────────────────────────────────────────────────────────┐
│                      前端展示层                          │
│         Vue 3 + Element Plus + SSE 流式展示             │
└────────────────────┬────────────────────────────────────┘
                     │ REST API / SSE
┌────────────────────▼────────────────────────────────────┐
│                    控制器层                              │
│    Agent │ Tool │ Workflow │ RAG │ Multi-Agent          │
└────────────────────┬────────────────────────────────────┘
                     │
┌────────────────────▼────────────────────────────────────┐
│                    服务层                                │
│  AgentService │ StreamingService │ MonitorService       │
└────────────────────┬────────────────────────────────────┘
                     │
┌────────────────────▼────────────────────────────────────┐
│           Spring AI Alibaba 框架层                       │
│ ┌─────────┐  ┌─────────┐  ┌──────────┐  ┌──────────┐  │
│ │ReactAgent│ │  Hooks  │  │Interceptors│ │StateGraph│  │
│ └─────────┘  └─────────┘  └──────────┘  └──────────┘  │
└────────────────────┬────────────────────────────────────┘
                     │
┌────────────────────▼────────────────────────────────────┐
│                数据与缓存层                              │
│         MySQL (JPA) │ Redis (缓存/检查点)               │
└─────────────────────────────────────────────────────────┘
```

### 技术栈一览

<table>
<tr>
<td width="50%">

**后端技术**
```
Spring Boot           3.2.1
Spring AI Alibaba     1.0.0-M2.3
MySQL                 8.0+
Redis                 7.0+
阿里云百炼            通义千问 + 通义万相
```

</td>
<td width="50%">

**前端技术**
```
Vue                   3.4
Element Plus          2.5+
Axios                 1.6+
Vue Router            4.2+
Vite                  5.0+
```

</td>
</tr>
</table>

---

## 📦 项目结构

```
agent/
├── src/main/java/com/kinch/agent/
│   ├── config/                 # 配置类
│   │   ├── AgentConfig.java    # ⭐ 核心Agent配置
│   │   ├── SecurityConfig.java # 安全配置
│   │   └── WebConfig.java      # Web配置
│   ├── hook/                   # ⭐ 框架Hooks
│   │   ├── CustomAuditHook.java        # 审计日志
│   │   ├── CustomSecurityHook.java     # 安全检查
│   │   ├── CustomTokenLimitHook.java   # Token限制
│   │   └── MessageTrimmingHook.java    # 消息修剪
│   ├── interceptor/            # ⭐ 框架Interceptors
│   │   ├── PerformanceInterceptor.java # 性能监控
│   │   ├── SafetyInterceptor.java      # 安全拦截
│   │   └── ToolMonitorInterceptor.java # 工具监控
│   ├── tool/                   # 工具实现
│   │   ├── WeatherTool.java            # 天气查询
│   │   ├── NamingTool.java             # 命名助手
│   │   ├── ScriptGenerationTool.java   # 脚本生成
│   │   ├── DatabaseQueryTool.java      # 数据库查询
│   │   ├── ApiCallTool.java            # API调用
│   │   └── FileOperationTool.java      # 文件操作
│   ├── workflow/               # ⭐ StateGraph工作流
│   │   └── RealStateGraphWorkflow.java # 工作流编排
│   ├── service/                # 业务服务
│   │   ├── AgentService.java           # Agent核心服务
│   │   ├── StreamingService.java       # 流式响应
│   │   ├── RagService.java             # RAG服务
│   │   ├── MultiAgentService.java      # Multi-Agent
│   │   ├── HumanInLoopService.java     # 人机协同
│   │   └── MonitorService.java         # 监控服务
│   ├── controller/             # REST控制器
│   ├── entity/                 # JPA实体
│   └── repository/             # 数据访问
└── frontend/                   # Vue 3 前端
    └── src/
        ├── views/              # 页面组件
        │   ├── Chat.vue                # 对话界面
        │   ├── FrameworkCenter.vue     # 框架中心
        │   ├── RagDemo.vue             # RAG演示
        │   ├── MultiAgentDemo.vue      # Multi-Agent演示
        │   └── HumanInLoopDemo.vue     # 人机协同演示
        └── api/                # API客户端
```

---

## 📊 监控与可观测性

### 实时指标监控

```bash
GET /api/monitor/stats

{
  "totalModelCalls": 1523,          // 总模型调用次数
  "totalModelTimeMs": 45678,        // 总耗时（毫秒）
  "avgModelTimeMs": 30.0,           // 平均耗时
  "totalToolCalls": 856,            // 总工具调用
  "successfulToolCalls": 831,       // 成功次数
  "failedToolCalls": 25,            // 失败次数
  "totalSecurityChecks": 1523,      // 安全检查次数
  "passedSecurityChecks": 1520,     // 通过次数
  "failedSecurityChecks": 3         // 拦截次数
}
```

### 完整审计日志

每次AI交互都会记录：
- ⏱️ 时间戳与执行时长
- 👤 用户信息与IP地址
- 📝 输入输出完整内容
- 🔧 工具调用与执行结果
- 💰 Token消耗与成本估算
- ❌ 错误信息与堆栈跟踪

---

## 🎯 应用场景

### 1. 智能对话

```java
ReactAgent agent = agentFactory.createAgent("chat_agent");
AssistantMessage response = agent.call("北京今天天气怎么样？", config);
// Agent自动调用WeatherTool并返回自然语言回复
```

### 2. RAG知识库问答

```java
// 上传文档
ragService.uploadDocument(pdfFile);

// 查询知识库
String answer = ragService.query("公司的年假政策是什么？");
// 返回: "根据公司政策，正式员工享有10天年假..."
// 来源: 员工手册.pdf（第23页）
```

### 3. 多智能体协作

```java
// 监督者将任务分配给专业智能体
MultiAgentResult result = multiAgentService.executeSupervisor(
    "分析这段代码并提出改进建议",
    List.of(codeReviewAgent, securityAgent, performanceAgent)
);
```

### 4. 复杂工作流编排

```java
StateGraph<OverAllState> graph = workflow.createDevelopmentWorkflow();
Map<String, Object> result = graph.invoke("创建用户注册API");
// 执行流程: 需求分析 → 设计 → 代码生成 → 测试 → 审核
```

---

## 🔐 安全特性

- 🔒 JWT身份认证与授权
- 🛡️ SQL注入防护
- 🚫 敏感词过滤
- 🔍 PII检测与脱敏
- 💳 Token配额强制执行
- 📋 完整审计追踪

---

## 🐳 部署方案

### Docker Compose 部署

```bash
docker-compose up -d
```

### Kubernetes 部署

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: spring-ai-agent
spec:
  replicas: 3
  template:
    spec:
      containers:
      - name: agent
        image: zhulaoqi/agent:latest
        env:
        - name: DASHSCOPE_API_KEY
          valueFrom:
            secretKeyRef:
              name: ai-secrets
              key: api-key
```

---

## 🤝 参与贡献

欢迎贡献代码！请遵循以下步骤：

1. 🍴 Fork 本仓库
2. 🌿 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. ✅ 提交你的更改 (`git commit -m 'Add AmazingFeature'`)
4. 📤 推送到分支 (`git push origin feature/AmazingFeature`)
5. 🔀 提交 Pull Request

阅读 [贡献指南](CONTRIBUTING.md) 了解详情。

---

## 📄 开源协议

本项目采用 **MIT 协议** - 查看 [LICENSE](LICENSE) 文件了解详情。

---

## 🙏 致谢

特别感谢以下项目和团队：

- [Spring AI Alibaba 团队](https://github.com/alibaba/spring-ai-alibaba) - 提供强大的AI框架
- [阿里云百炼](https://www.aliyun.com/) - 提供通义千问和通义万相模型
- [Spring Boot 团队](https://spring.io/) - 提供稳定的基础框架
- [Vue.js 团队](https://vuejs.org/) - 提供优秀的前端框架

---

## ⭐ Star History

如果这个项目对你有帮助，请给个 Star！⭐

[![Star History Chart](https://api.star-history.com/svg?repos=zhulaoqi/agent&type=Date)](https://star-history.com/#zhulaoqi/agent&Date)

---

## 📈 开发路线图

- [x] 核心 ReactAgent 实现
- [x] Hooks & Interceptors 完整集成
- [x] StateGraph 工作流编排
- [x] RAG 知识库集成
- [x] Multi-Agent 多智能体协作
- [x] Human-in-the-Loop 人机协同
- [x] 监控与审计体系
- [ ] 向量数据库集成（Milvus）
- [ ] 高级RAG策略
- [ ] 多模态支持（视觉、音频）
- [ ] GraphQL API
- [ ] Kubernetes Operator
- [ ] 性能基准测试

---

<div align="center">

**用 ❤️ 和 Spring AI Alibaba 构建**

[⬆ 返回顶部](#-spring-ai-alibaba-agent-框架)

</div>
