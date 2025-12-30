<template>
  <div class="simple-layout">
    <!-- 顶部导航栏 -->
    <header class="header">
      <div class="header-left">
        <h1>🤖 AI Agent</h1>
        <span class="subtitle">Spring AI Alibaba</span>
      </div>
      
      <nav class="nav-tabs">
        <router-link to="/chat" class="nav-tab">💬 对话</router-link>
        <router-link to="/workflow" class="nav-tab">⚙️ 工作流</router-link>
        <router-link to="/naming" class="nav-tab">📝 命名</router-link>
        <router-link to="/script" class="nav-tab">📄 脚本</router-link>
        <router-link to="/image-gen" class="nav-tab">🎨 图片</router-link>
      </nav>
      
      <div class="header-right">
        <span class="user-name">{{ userName }}</span>
        <button @click="handleLogout" class="logout-btn">退出</button>
      </div>
    </header>

    <!-- 主内容区 -->
    <main class="main-content">
      <slot></slot>
    </main>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userName = ref(localStorage.getItem('username') || '用户')

const handleLogout = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('username')
  ElMessage.success('已退出登录')
  router.push('/login')
}
</script>

<style scoped>
.simple-layout {
  width: 100%;
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f5f7fa;
}

/* 顶部导航 */
.header {
  height: 60px;
  background: #fff;
  border-bottom: 1px solid #e4e7ed;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.05);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.header-left h1 {
  font-size: 20px;
  font-weight: 600;
  color: #303133;
  margin: 0;
}

.subtitle {
  font-size: 12px;
  color: #909399;
  padding: 2px 8px;
  background: #f0f2f5;
  border-radius: 4px;
}

/* 导航标签 */
.nav-tabs {
  display: flex;
  gap: 8px;
}

.nav-tab {
  padding: 8px 16px;
  text-decoration: none;
  color: #606266;
  border-radius: 6px;
  font-size: 14px;
  transition: all 0.2s;
}

.nav-tab:hover {
  background: #f5f7fa;
  color: #409eff;
}

.nav-tab.router-link-active {
  background: #ecf5ff;
  color: #409eff;
  font-weight: 500;
}

/* 右侧用户区 */
.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-name {
  font-size: 14px;
  color: #606266;
}

.logout-btn {
  padding: 6px 16px;
  background: #f5f7fa;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  color: #606266;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
}

.logout-btn:hover {
  background: #fff;
  border-color: #409eff;
  color: #409eff;
}

/* 主内容区 */
.main-content {
  flex: 1;
  overflow: hidden;
}
</style>



