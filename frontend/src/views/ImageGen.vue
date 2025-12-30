<template>
  <div class="page-container">
    <div class="page-header">
      <h1><el-icon><Picture /></el-icon> AI图片生成</h1>
      <p>使用DashScope万象模型生成图片</p>
    </div>

    <div class="content-area">
      <div class="input-section">
        <el-input
          v-model="prompt"
          type="textarea"
          :rows="4"
          placeholder="描述你想要生成的图片，例如：一只可爱的猫咪在花园里玩耍"
          @keydown.ctrl.enter="generateImage"
        />
        <el-button 
          type="primary" 
          @click="generateImage"
          :loading="loading"
          :disabled="!prompt.trim()"
          size="large"
        >
          <el-icon><MagicStick /></el-icon> 生成图片
        </el-button>
      </div>

      <div v-if="imageUrl" class="result-section">
        <h3>生成结果</h3>
        <div class="image-container">
          <el-image 
            :src="imageUrl" 
            fit="contain"
            :preview-src-list="[imageUrl]"
          />
        </div>
        <el-button @click="downloadImage">
          <el-icon><Download /></el-icon> 下载图片
        </el-button>
      </div>

      <div v-if="error" class="error-message">
        <el-alert type="error" :title="error" :closable="false" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import api from '@/api/agent'

const prompt = ref('')
const imageUrl = ref('')
const loading = ref(false)
const error = ref('')

const generateImage = async () => {
  if (!prompt.value.trim()) return

  loading.value = true
  error.value = ''
  
  try {
    const result = await api.generateImage(prompt.value)
    if (result.success && result.imageUrl) {
      imageUrl.value = result.imageUrl
      ElMessage.success('图片生成成功')
    } else {
      error.value = result.message || '图片生成失败'
    }
  } catch (err) {
    error.value = '图片生成失败：' + err.message
    ElMessage.error('图片生成失败')
  } finally {
    loading.value = false
  }
}

const downloadImage = () => {
  const link = document.createElement('a')
  link.href = imageUrl.value
  link.download = 'ai-generated-image.png'
  link.click()
}
</script>

<style scoped>
.page-container {
  padding: 40px;
  max-width: 1200px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 32px;
}

.page-header h1 {
  color: #fff;
  font-size: 32px;
  margin-bottom: 8px;
  display: flex;
  align-items: center;
  gap: 12px;
}

.page-header p {
  color: rgba(255, 255, 255, 0.6);
  font-size: 14px;
}

.content-area {
  background: rgba(138, 43, 226, 0.05);
  border: 1px solid rgba(138, 43, 226, 0.2);
  border-radius: 12px;
  padding: 32px;
}

.input-section {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.result-section {
  margin-top: 32px;
  padding-top: 32px;
  border-top: 1px solid rgba(138, 43, 226, 0.2);
}

.result-section h3 {
  color: #fff;
  margin-bottom: 16px;
}

.image-container {
  background: rgba(0, 0, 0, 0.3);
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 16px;
}

.error-message {
  margin-top: 16px;
}
</style>
