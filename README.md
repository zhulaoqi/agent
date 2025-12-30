# AI Agent 前端

基于 Vue 3 + Vite + Element Plus 的现代化前端应用

## 技术栈

- Vue 3 - 渐进式JavaScript框架
- Vite - 下一代前端构建工具
- Element Plus - Vue 3 UI组件库
- Vue Router - 官方路由管理器
- Pinia - Vue状态管理
- Axios - HTTP客户端

## 快速开始

### 安装依赖

```bash
cd frontend
npm install
```

### 开发模式

```bash
npm run dev
```

访问: http://localhost:3000

### 生产构建

```bash
npm run build
```

构建产物会输出到 `../src/main/resources/static`

## 项目结构

```
frontend/
├── src/
│   ├── api/              # API接口
│   │   └── agent.js
│   ├── views/            # 页面组件
│   │   ├── Chat.vue        # 对话页面
│   │   ├── NamingHelper.vue # 命名助手
│   │   ├── ScriptGen.vue    # 脚本生成
│   │   └── History.vue      # 历史记录
│   ├── router/           # 路由配置
│   ├── App.vue           # 根组件
│   └── main.js           # 入口文件
├── index.html
├── vite.config.js
└── package.json
```

## 功能特性

- ✅ 实时对话
- ✅ 命名助手
- ✅ 脚本生成
- ✅ 极简黑白灰设计
- ✅ 响应式布局
- ✅ 流畅动画

## 设计风格

- 主色调：黑(#2c2c2c)、白(#ffffff)、灰(#f5f5f5)
- 点缀色：蓝(#409EFF)、红(#F56C6C)、绿(#67C23A)
- 极简主义设计理念
- 卡片式布局



