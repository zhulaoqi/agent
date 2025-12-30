<template>
  <div class="container">
    <aside class="sidebar">
      <div class="logo">
        <el-icon :size="32"><Document /></el-icon>
        <h2>脚本生成</h2>
      </div>
      <nav class="nav-menu">
        <router-link to="/chat" class="nav-item">
          <el-icon><ChatDotRound /></el-icon>
          <span>对话</span>
        </router-link>
        <router-link to="/naming" class="nav-item">
          <el-icon><Edit /></el-icon>
          <span>命名助手</span>
        </router-link>
        <router-link to="/script" class="nav-item active">
          <el-icon><Document /></el-icon>
          <span>脚本生成</span>
        </router-link>
      </nav>
    </aside>

    <main class="main-content">
      <div class="content-wrapper">
        <h1>运维脚本生成</h1>
        <p class="subtitle">为运维人员生成Shell、Python等自动化脚本</p>

        <el-card>
          <el-form :model="form" label-width="100px">
            <el-form-item label="脚本类型">
              <el-radio-group v-model="form.type">
                <el-radio label="shell">Shell</el-radio>
                <el-radio label="python">Python</el-radio>
                <el-radio label="powershell">PowerShell</el-radio>
              </el-radio-group>
            </el-form-item>

            <el-form-item label="需求描述">
              <el-input
                v-model="form.requirement"
                type="textarea"
                :rows="4"
                placeholder="例如：批量备份/var/log目录下的日志文件到/backup目录..."
              />
            </el-form-item>

            <el-form-item>
              <el-button type="primary" @click="generate" :loading="loading">
                生成脚本
              </el-button>
            </el-form-item>
          </el-form>
        </el-card>

        <el-card v-if="scriptContent" class="result-card">
          <template #header>
            <div class="card-header">
              <span>生成的脚本</span>
              <el-button size="small" @click="copyScript">复制</el-button>
            </div>
          </template>
          <pre class="script-content"><code>{{ scriptContent }}</code></pre>
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
  type: 'shell',
  requirement: ''
})

const scriptContent = ref('')
const loading = ref(false)

const generate = async () => {
  if (!form.value.requirement) {
    ElMessage.warning('请输入需求描述')
    return
  }

  loading.value = true
  const prompt = `帮我生成一个${form.value.type}脚本：${form.value.requirement}`

  try {
    const response = await api.chat(prompt)
    if (response.success) {
      scriptContent.value = extractScript(response.message)
      ElMessage.success('脚本生成成功')
    }
  } catch (error) {
    ElMessage.error('生成失败: ' + error.message)
  } finally {
    loading.value = false
  }
}

const extractScript = (text) => {
  const codeMatch = text.match(/```[\s\S]*?```/)
  if (codeMatch) {
    return codeMatch[0].replace(/```[a-z]*\n?/g, '').replace(/```/g, '').trim()
  }
  return text
}

const copyScript = () => {
  navigator.clipboard.writeText(scriptContent.value)
  ElMessage.success('已复制脚本')
}
</script>

<style scoped>
.container {
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
  max-width: 1000px;
  margin: 0 auto;
  padding: 40px 20px;
}

.script-content {
  background: #1e1e1e;
  color: #d4d4d4;
  padding: 20px;
  border-radius: 4px;
  overflow-x: auto;
  font-family: 'Courier New', monospace;
  font-size: 14px;
  line-height: 1.6;
}
</style>

