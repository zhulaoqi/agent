<div align="center">

# 🤖 Spring AI Alibaba Agent Framework

**Enterprise-Grade AI Agent System Built on Spring AI Alibaba**

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.1-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Spring AI Alibaba](https://img.shields.io/badge/Spring%20AI%20Alibaba-1.0.0--M2.3-blue.svg)](https://github.com/alibaba/spring-ai-alibaba)
[![Vue](https://img.shields.io/badge/Vue-3.4-42b883.svg)](https://vuejs.org/)
[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)
[![PRs Welcome](https://img.shields.io/badge/PRs-welcome-brightgreen.svg)](CONTRIBUTING.md)

**[Features](#-features) • [Quick Start](#-quick-start) • [Architecture](#-architecture) • [Documentation](#-documentation) • [Demo](#-demo)**

</div>

---

## 📖 Overview

A **production-ready AI Agent system** that showcases the complete capabilities of the Spring AI Alibaba framework. This project demonstrates enterprise-grade patterns including ReactAgent, Hooks & Interceptors, StateGraph workflows, Multi-Agent orchestration, and RAG implementations.

### 🎯 Why This Project?

| Challenge | Solution | Implementation |
|-----------|----------|----------------|
| **No Observability** | Full audit trail of all AI operations | `CustomAuditHook` + `MonitorService` |
| **Security Risks** | Multi-layer security checks | `CustomSecurityHook` + `SafetyInterceptor` |
| **Cost Overruns** | Token quota management | `CustomTokenLimitHook` |
| **Complex Workflows** | Visual workflow orchestration | `StateGraph` with conditional routing |
| **Black Box AI** | Human-in-the-loop approvals | `HumanInLoopService` |
| **Context Overflow** | Intelligent message trimming | `MessageTrimmingHook` |

---

## ✨ Features

### 🤖 Core Agent Capabilities

<table>
<tr>
<td width="50%">

#### ReactAgent
- 🧠 Intelligent conversation with context
- 🛠️ Automatic tool selection & execution
- 💾 Persistent memory management
- 📊 Structured output formatting
- ⚡ Streaming responses (SSE)

</td>
<td width="50%">

#### Advanced Patterns
- 📚 **RAG**: Knowledge base Q&A
- 👥 **Multi-Agent**: Supervisor mode
- 🤝 **Human-in-Loop**: Approval workflows
- 🔀 **StateGraph**: Complex orchestration
- 🎨 **AI Art**: Text-to-image generation

</td>
</tr>
</table>

### 🎯 Hooks & Interceptors (Framework Deep Dive)

Our implementation showcases the full power of Spring AI Alibaba's extensibility:

```java
ReactAgent agent = ReactAgent.builder()
    .name("production_agent")
    .model(chatModel)
    .tools(weatherTool, databaseTool, apiTool)
    .hooks(auditHook, securityHook, tokenLimitHook)
    .interceptors(performanceInterceptor, safetyInterceptor)
    .build();
```

<details>
<summary><b>📋 Complete Hooks & Interceptors List</b></summary>

| Type | Class | Purpose | Metrics Tracked |
|------|-------|---------|----------------|
| **ModelHook** | `CustomAuditHook` | Audit logging | Time, tokens, I/O |
| **ModelHook** | `CustomSecurityHook` | Security validation | SQL injection, PII |
| **ModelHook** | `CustomTokenLimitHook` | Quota management | User limits, costs |
| **MessagesModelHook** | `MessageTrimmingHook` | Context optimization | Message count |
| **ModelInterceptor** | `PerformanceInterceptor` | Performance monitoring | Latency, throughput |
| **ModelInterceptor** | `SafetyInterceptor` | Content filtering | Unsafe content |
| **ToolInterceptor** | `ToolMonitorInterceptor` | Tool analytics | Success rate, timing |

</details>

### 🛠️ Rich Tool Ecosystem

| Tool | Description | Use Case |
|------|-------------|----------|
| 🌤️ **WeatherTool** | Real-time weather data | "What's the weather in Beijing?" |
| 🏷️ **NamingTool** | Code naming assistant | "Generate Java class name for user service" |
| 📝 **ScriptTool** | DevOps script generation | "Create a log backup bash script" |
| 🗄️ **DatabaseTool** | Safe SQL execution | "Query top 10 users" |
| 🌐 **ApiTool** | HTTP request handler | "Call REST API with auth" |
| 📁 **FileTool** | File operations | "Read config.json" |
| 🎨 **ImageTool** | AI image generation | "Create a sunset landscape" |

### 🔀 StateGraph Workflows

True graph-based workflow orchestration (not simplified):

```
┌─────────────┐
│   Classify  │
│  Requirement│
└──────┬──────┘
       │
   ┌───┴────┐
   │        │
   ▼        ▼
┌──────┐ ┌──────┐
│Quick │ │Detail│
│AnalysisGenerate│
└──┬───┘ └───┬──┘
   │         │
   └────┬────┘
        ▼
   ┌─────────┐
   │Generate │
   │  Code   │
   └─────────┘
```

**Supported Features:**
- ✅ Dynamic node routing
- ✅ Conditional edges
- ✅ State management across nodes
- ✅ Human approval nodes
- ✅ Error handling & retry

---

## 🚀 Quick Start

### Prerequisites

```bash
☕ Java 17+          🐬 MySQL 8.0+
📦 Maven 3.8+        🔴 Redis 7.0+
🟢 Node.js 18+       🔑 Alibaba Cloud API Key
```

### Installation (5 minutes)

```bash
# 1. Clone repository
git clone https://github.com/zhulaoqi/agent.git
cd agent

# 2. Database setup
mysql -u root -p -e "CREATE DATABASE agent_db CHARACTER SET utf8mb4;"
mysql -u root -p agent_db < src/main/resources/sql/init.sql

# 3. Configure API key
export DASHSCOPE_API_KEY=sk-your-key-here

# 4. Start backend
mvn spring-boot:run

# 5. Start frontend (new terminal)
cd frontend && npm install && npm run dev
```

**Access:**
- 🌐 Frontend: http://localhost:5173
- 🔌 Backend: http://localhost:8080
- 📖 API Docs: http://localhost:8080/swagger-ui.html

### First Request

```bash
curl -X POST http://localhost:8080/api/agent/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "What'\''s the weather in Shanghai?"}'
```

---

## 🏗️ Architecture

### System Architecture

```
┌─────────────────────────────────────────────────────────┐
│                    Frontend Layer                        │
│         Vue 3 + Element Plus + SSE Streaming            │
└────────────────────┬────────────────────────────────────┘
                     │ REST API / SSE
┌────────────────────▼────────────────────────────────────┐
│                  Controller Layer                        │
│    Agent │ Tool │ Workflow │ RAG │ Multi-Agent          │
└────────────────────┬────────────────────────────────────┘
                     │
┌────────────────────▼────────────────────────────────────┐
│                  Service Layer                           │
│  AgentService │ StreamingService │ MonitorService       │
└────────────────────┬────────────────────────────────────┘
                     │
┌────────────────────▼────────────────────────────────────┐
│           Spring AI Alibaba Framework                    │
│ ┌─────────┐  ┌─────────┐  ┌──────────┐  ┌──────────┐  │
│ │ReactAgent│ │  Hooks  │  │Interceptors│ │StateGraph│  │
│ └─────────┘  └─────────┘  └──────────┘  └──────────┘  │
└────────────────────┬────────────────────────────────────┘
                     │
┌────────────────────▼────────────────────────────────────┐
│              Data & Cache Layer                          │
│         MySQL (JPA) │ Redis (Cache/Checkpoint)          │
└─────────────────────────────────────────────────────────┘
```

### Technology Stack

<table>
<tr>
<td width="50%">

**Backend**
```
Spring Boot           3.2.1
Spring AI Alibaba     1.0.0-M2.3
MySQL                 8.0+
Redis                 7.0+
Alibaba Cloud         Qwen + Wanx
```

</td>
<td width="50%">

**Frontend**
```
Vue                   3.4
Element Plus          2.5+
Axios                 1.6+
Vue Router            4.2+
```

</td>
</tr>
</table>

---

## 📦 Project Structure

```
agent/
├── src/main/java/com/kinch/agent/
│   ├── config/                 # Configuration
│   │   ├── AgentConfig.java    # ⭐ Core agent setup
│   │   ├── SecurityConfig.java
│   │   └── WebConfig.java
│   ├── hook/                   # ⭐ Framework hooks
│   │   ├── CustomAuditHook.java
│   │   ├── CustomSecurityHook.java
│   │   ├── CustomTokenLimitHook.java
│   │   └── MessageTrimmingHook.java
│   ├── interceptor/            # ⭐ Framework interceptors
│   │   ├── PerformanceInterceptor.java
│   │   ├── SafetyInterceptor.java
│   │   └── ToolMonitorInterceptor.java
│   ├── tool/                   # Tool implementations
│   │   ├── WeatherTool.java
│   │   ├── NamingTool.java
│   │   ├── ScriptGenerationTool.java
│   │   ├── DatabaseQueryTool.java
│   │   ├── ApiCallTool.java
│   │   └── FileOperationTool.java
│   ├── workflow/               # ⭐ StateGraph workflows
│   │   └── RealStateGraphWorkflow.java
│   ├── service/                # Business services
│   │   ├── AgentService.java
│   │   ├── StreamingService.java
│   │   ├── RagService.java
│   │   ├── MultiAgentService.java
│   │   ├── HumanInLoopService.java
│   │   └── MonitorService.java
│   ├── controller/             # REST controllers
│   ├── entity/                 # JPA entities
│   └── repository/             # Data access
└── frontend/                   # Vue 3 frontend
    └── src/
        ├── views/              # Page components
        │   ├── Chat.vue
        │   ├── FrameworkCenter.vue
        │   ├── RagDemo.vue
        │   ├── MultiAgentDemo.vue
        │   └── HumanInLoopDemo.vue
        └── api/                # API clients
```

---

## 📊 Monitoring & Observability

### Real-time Metrics

```bash
GET /api/monitor/stats

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

### Audit Logging

Every AI interaction is logged with:
- ⏱️ Timestamp & duration
- 👤 User info & IP address
- 📝 Input/output content
- 🔧 Tool calls & results
- 💰 Token consumption & cost
- ❌ Errors & exceptions

---

## 🎯 Use Cases

### 1. Conversational AI

```java
ReactAgent agent = agentFactory.createAgent("chat_agent");
AssistantMessage response = agent.call("What's the weather in Beijing?", config);
// Agent automatically calls WeatherTool and returns natural language response
```

### 2. RAG Knowledge Base

```java
// Upload documents
ragService.uploadDocument(pdfFile);

// Query with context
String answer = ragService.query("What's our vacation policy?");
// Returns: "According to company policy, employees get 10 days annual leave..."
// Source: employee_handbook.pdf (Page 23)
```

### 3. Multi-Agent Collaboration

```java
// Supervisor delegates tasks to specialized agents
MultiAgentResult result = multiAgentService.executeSupervisor(
    "Analyze this code and suggest improvements",
    List.of(codeReviewAgent, securityAgent, performanceAgent)
);
```

### 4. Complex Workflows

```java
StateGraph<OverAllState> graph = workflow.createDevelopmentWorkflow();
Map<String, Object> result = graph.invoke("Create user registration API");
// Executes: Requirement Analysis → Design → Code Gen → Testing → Review
```

---

## 🔐 Security Features

- 🔒 JWT authentication & authorization
- 🛡️ SQL injection prevention
- 🚫 Sensitive word filtering
- 🔍 PII detection & masking
- 💳 Token quota enforcement
- 📋 Complete audit trail

---

## 🐳 Deployment

### Docker Compose

```bash
docker-compose up -d
```

### Kubernetes

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

## 📚 Documentation

- 📖 [Quick Start Guide](docs/quickstart.md)
- 🏗️ [Architecture Deep Dive](docs/architecture.md)
- 🔌 [API Reference](docs/api.md)
- 🎓 [Best Practices](docs/best-practices.md)
- 🚀 [Deployment Guide](docs/deployment.md)

---

## 🤝 Contributing

Contributions are welcome! Please follow these steps:

1. 🍴 Fork the repository
2. 🌿 Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. ✅ Commit your changes (`git commit -m 'Add AmazingFeature'`)
4. 📤 Push to the branch (`git push origin feature/AmazingFeature`)
5. 🔀 Open a Pull Request

Read our [Contributing Guidelines](CONTRIBUTING.md) for details.

---

## 📄 License

This project is licensed under the **MIT License** - see the [LICENSE](LICENSE) file for details.

---

## 🙏 Acknowledgments

Special thanks to:

- [Spring AI Alibaba Team](https://github.com/alibaba/spring-ai-alibaba) - For the amazing framework
- [Alibaba Cloud](https://www.aliyun.com/) - For Qwen & Wanx AI models
- [Spring Boot Team](https://spring.io/) - For the robust foundation
- [Vue.js Team](https://vuejs.org/) - For the reactive frontend framework

---

## 📮 Contact & Support

- 👤 **Author**: [@zhulaoqi](https://github.com/zhulaoqi)
- 📧 **Email**: your-email@example.com
- 🐛 **Issues**: [GitHub Issues](https://github.com/zhulaoqi/agent/issues)
- 💬 **Discussions**: [GitHub Discussions](https://github.com/zhulaoqi/agent/discussions)

---

## ⭐ Star History

If you find this project helpful, please consider giving it a star! ⭐

[![Star History Chart](https://api.star-history.com/svg?repos=zhulaoqi/agent&type=Date)](https://star-history.com/#zhulaoqi/agent&Date)

---

## 📈 Roadmap

- [x] Core ReactAgent implementation
- [x] Hooks & Interceptors
- [x] StateGraph workflows
- [x] RAG integration
- [x] Multi-Agent orchestration
- [x] Human-in-the-Loop
- [x] Monitoring & auditing
- [ ] Vector database integration (Milvus)
- [ ] Advanced RAG strategies
- [ ] Multi-modal support (vision, audio)
- [ ] GraphQL API
- [ ] Kubernetes operators
- [ ] Performance benchmarks

---

<div align="center">

**Built with ❤️ using Spring AI Alibaba**

[⬆ Back to Top](#-spring-ai-alibaba-agent-framework)

</div>
