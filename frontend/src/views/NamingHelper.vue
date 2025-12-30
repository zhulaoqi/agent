<template>
  <div class="naming-container">
    <aside class="sidebar">
      <div class="logo">
        <el-icon :size="32"><Edit /></el-icon>
        <h2>命名助手</h2>
      </div>
      <nav class="nav-menu">
        <router-link to="/chat" class="nav-item">
          <el-icon><ChatDotRound /></el-icon>
          <span>对话</span>
        </router-link>
        <router-link to="/naming" class="nav-item active">
          <el-icon><Edit /></el-icon>
          <span>命名助手</span>
        </router-link>
        <router-link to="/script" class="nav-item">
          <el-icon><Document /></el-icon>
          <span>脚本生成</span>
        </router-link>
      </nav>
    </aside>

    <main class="main-content">
      <div class="content-wrapper">
        <h1>编程命名助手</h1>
        <p class="subtitle">为技术人员提供专业的变量名、类名、方法名建议</p>

        <el-card class="form-card">
          <el-form :model="form" label-width="100px">
            <el-form-item label="编程语言">
              <el-select v-model="form.language" placeholder="选择语言">
                <el-option label="Java" value="java" />
                <el-option label="Python" value="python" />
                <el-option label="JavaScript" value="javascript" />
                <el-option label="Go" value="go" />
              </el-select>
            </el-form-item>

            <el-form-item label="命名类型">
              <el-select v-model="form.type" placeholder="选择类型">
                <el-option label="类名" value="class" />
                <el-option label="方法名" value="method" />
                <el-option label="变量名" value="variable" />
                <el-option label="常量名" value="constant" />
              </el-select>
            </el-form-item>

            <el-form-item label="功能描述">
              <el-input
                v-model="form.description"
                type="textarea"
                :rows="3"
                placeholder="例如：用户管理服务、查询用户信息的方法..."
              />
            </el-form-item>

            <el-form-item>
              <el-button type="primary" @click="generate" :loading="loading">
                生成命名建议
              </el-button>
            </el-form-item>
          </el-form>
        </el-card>

        <el-card v-if="suggestions.length > 0" class="result-card">
          <template #header>
            <div class="card-header">
              <span>命名建议</span>
              <el-tag>{{ suggestions.length }} 个建议</el-tag>
            </div>
          </template>
          <div class="suggestions-list">
            <div v-for="(item, index) in suggestions" :key="index" 
                 class="suggestion-item"
                 @click="copySuggestion(item)">
              <span class="index">{{ index + 1 }}</span>
              <code class="name">{{ item }}</code>
              <el-icon class="copy-icon"><CopyDocument /></el-icon>
            </div>
          </div>
        </el-card>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import api from '@/api/agent'

const form = ref({
  language: 'java',
  type: 'class',
  description: ''
})

const suggestions = ref([])
const loading = ref(false)

const generate = async () => {
  if (!form.value.description) {
    ElMessage.warning('请输入功能描述')
    return
  }

  loading.value = true
  const prompt = `帮我生成${form.value.language}语言的${form.value.type}命名建议：${form.value.description}`

  try {
    const response = await api.chat(prompt)
    if (response.success) {
      // 解析命名建议
      const names = extractNames(response.message)
      suggestions.value = names
      ElMessage.success(`生成了 ${names.length} 个命名建议`)
    }
  } catch (error) {
    ElMessage.error('生成失败: ' + error.message)
  } finally {
    loading.value = false
  }
}

const extractNames = (text) => {
  const matches = text.match(/[A-Z][a-zA-Z0-9_]*/g) || []
  return [...new Set(matches)].slice(0, 10)
}

const copySuggestion = (name) => {
  navigator.clipboard.writeText(name)
  ElMessage.success('已复制: ' + name)
}
</script>

<style scoped>
.naming-container {
  display: flex;
  height: 100vh;
}

.sidebar {
  width: 240px;
  background: #2c2c2c;
  color: #ffffff;
}

.logo {
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 12px;
  border-bottom: 1px solid #3c3c3c;
}

.nav-menu {
  padding: 12px 0;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 20px;
  color: #a0a0a0;
  text-decoration: none;
}

.nav-item.active {
  background: #409EFF;
  color: #ffffff;
}

.main-content {
  flex: 1;
  background: #f5f5f5;
  overflow-y: auto;
}

.content-wrapper {
  max-width: 800px;
  margin: 0 auto;
  padding: 40px 20px;
}

h1 {
  font-size: 28px;
  margin-bottom: 8px;
}

.subtitle {
  color: #606266;
  margin-bottom: 32px;
}

.form-card, .result-card {
  margin-bottom: 20px;
}

.suggestions-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.suggestion-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: #f5f5f5;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s;
}

.suggestion-item:hover {
  background: #e0e0e0;
}

.index {
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #409EFF;
  color: white;
  border-radius: 50%;
  font-size: 12px;
}

.name {
  flex: 1;
  font-family: 'Courier New', monospace;
  color: #303133;
}

.copy-icon {
  color: #909399;
}
</style>


