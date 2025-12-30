<template>
  <SimpleLayout>
    <div class="workflow-page">
      <div class="workflow-content">
        <h1 class="page-title">⚙️ AI 工作流</h1>
        <p class="page-desc">选择工作流类型，AI将按步骤完成复杂任务</p>

        <!-- 工作流选择 -->
        <div class="workflow-grid">
          <div class="workflow-card" @click="selectedWorkflow = 'dev'">
            <div class="card-icon">💻</div>
            <h3>开发工作流</h3>
            <p>需求分析 → 方案设计 → 代码生成 → 测试</p>
            <div v-if="selectedWorkflow === 'dev'" class="card-selected">✓</div>
          </div>

          <div class="workflow-card" @click="selectedWorkflow = 'conditional'">
            <div class="card-icon">🔀</div>
            <h3>智能分支</h3>
            <p>自动识别问题类型，选择最优处理路径</p>
            <div v-if="selectedWorkflow === 'conditional'" class="card-selected">✓</div>
          </div>
        </div>

        <!-- 输入区 -->
        <div class="input-section">
          <label class="input-label">
            {{ selectedWorkflow === 'dev' ? '📝 需求描述' : '💬 问题描述' }}
          </label>
          <textarea
            v-model="inputText"
            :placeholder="selectedWorkflow === 'dev' 
              ? '例如：开发一个用户注册功能，包括邮箱验证和密码加密' 
              : '例如：如何优化MySQL查询性能？'"
            rows="4"
          ></textarea>
          
          <button @click="handleExecute" :disabled="!inputText.trim() || loading" class="execute-btn">
            <span v-if="!loading">{{ loading ? '执行中...' : '🚀 开始执行' }}</span>
            <span v-else>执行中...</span>
          </button>
        </div>

        <!-- 结果展示 -->
        <div v-if="result" class="result-section">
          <h3 class="result-title">📊 执行结果</h3>
          
          <!-- 开发工作流结果 -->
          <div v-if="result.state" class="result-steps">
            <div v-if="result.state.analysis" class="result-step">
              <div class="step-header">
                <span class="step-icon">📋</span>
                <span class="step-title">需求分析</span>
              </div>
              <div class="step-content">{{ result.state.analysis }}</div>
            </div>

            <div v-if="result.state.design" class="result-step">
              <div class="step-header">
                <span class="step-icon">🎨</span>
                <span class="step-title">方案设计</span>
              </div>
              <div class="step-content">{{ result.state.design }}</div>
            </div>

            <div v-if="result.state.code" class="result-step">
              <div class="step-header">
                <span class="step-icon">💻</span>
                <span class="step-title">代码生成</span>
              </div>
              <div class="step-content"><pre>{{ result.state.code }}</pre></div>
            </div>

            <div v-if="result.state.test_result" class="result-step">
              <div class="step-header">
                <span class="step-icon">✅</span>
                <span class="step-title">测试用例</span>
              </div>
              <div class="step-content">{{ result.state.test_result }}</div>
            </div>
          </div>

          <!-- 条件工作流结果 -->
          <div v-else-if="result.state && result.state.result" class="result-simple">
            <div class="result-category">
              <strong>分类：</strong>{{ result.state.category }}
            </div>
            <div class="result-answer">{{ result.state.result }}</div>
          </div>
        </div>
      </div>
    </div>
  </SimpleLayout>
</template>

<script setup>
import { ref } from 'vue'
import SimpleLayout from '../components/SimpleLayout.vue'
import axios from 'axios'
import { ElMessage } from 'element-plus'

const selectedWorkflow = ref('dev')
const inputText = ref('')
const result = ref(null)
const loading = ref(false)

const handleExecute = async () => {
  if (!inputText.value.trim()) return
  
  loading.value = true
  result.value = null
  
  try {
    const endpoint = selectedWorkflow.value === 'dev' 
      ? '/api/workflow/sequential'
      : '/api/workflow/conditional'
    
    const payload = selectedWorkflow.value === 'dev'
      ? { requirement: inputText.value }
      : { input: inputText.value }
    
    const response = await axios.post(endpoint, payload)
    
    if (response.data.success) {
      result.value = response.data.result
      ElMessage.success('工作流执行成功')
    } else {
      ElMessage.error(response.data.error || '执行失败')
    }
  } catch (error) {
    console.error('执行失败:', error)
    ElMessage.error('执行失败，请重试')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.workflow-page {
  height: 100%;
  overflow-y: auto;
  background: linear-gradient(135deg, #f5f7fa 0%, #e8ebf0 100%);
}

.workflow-content {
  max-width: 1000px;
  margin: 0 auto;
  padding: 40px 20px;
}

.page-title {
  font-size: 32px;
  color: #303133;
  margin: 0 0 8px 0;
  text-align: center;
}

.page-desc {
  color: #909399;
  text-align: center;
  margin: 0 0 40px 0;
}

/* 工作流卡片 */
.workflow-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 20px;
  margin-bottom: 32px;
}

.workflow-card {
  background: #fff;
  padding: 24px;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s;
  border: 2px solid transparent;
  position: relative;
  box-shadow: 0 2px 12px rgba(0,0,0,0.08);
}

.workflow-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0,0,0,0.12);
  border-color: #409eff;
}

.card-icon {
  font-size: 48px;
  margin-bottom: 12px;
}

.workflow-card h3 {
  font-size: 18px;
  color: #303133;
  margin: 0 0 8px 0;
}

.workflow-card p {
  color: #909399;
  font-size: 14px;
  margin: 0;
}

.card-selected {
  position: absolute;
  top: 12px;
  right: 12px;
  width: 32px;
  height: 32px;
  background: #67c23a;
  color: #fff;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  font-weight: bold;
}

/* 输入区 */
.input-section {
  background: #fff;
  padding: 24px;
  border-radius: 12px;
  margin-bottom: 24px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.08);
}

.input-label {
  display: block;
  font-size: 16px;
  font-weight: 500;
  color: #303133;
  margin-bottom: 12px;
}

textarea {
  width: 100%;
  padding: 12px 16px;
  border: 1px solid #dcdfe6;
  border-radius: 8px;
  font-size: 14px;
  font-family: inherit;
  resize: vertical;
  line-height: 1.6;
  margin-bottom: 16px;
}

textarea:focus {
  outline: none;
  border-color: #409eff;
}

.execute-btn {
  width: 100%;
  padding: 14px;
  background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%);
  color: #fff;
  border: none;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s;
}

.execute-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(64, 158, 255, 0.4);
}

.execute-btn:disabled {
  background: #a0cfff;
  cursor: not-allowed;
  transform: none;
}

/* 结果展示 */
.result-section {
  background: #fff;
  padding: 24px;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.08);
}

.result-title {
  font-size: 20px;
  color: #303133;
  margin: 0 0 20px 0;
}

.result-step {
  margin-bottom: 24px;
  padding-bottom: 24px;
  border-bottom: 1px solid #f0f2f5;
}

.result-step:last-child {
  margin-bottom: 0;
  padding-bottom: 0;
  border-bottom: none;
}

.step-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
}

.step-icon {
  font-size: 20px;
}

.step-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.step-content {
  padding: 12px 16px;
  background: #f5f7fa;
  border-radius: 8px;
  color: #606266;
  line-height: 1.8;
  white-space: pre-wrap;
}

.step-content pre {
  margin: 0;
  font-family: 'Monaco', 'Courier New', monospace;
  font-size: 13px;
  overflow-x: auto;
}

.result-simple {
  line-height: 1.8;
}

.result-category {
  padding: 12px;
  background: #f0f9ff;
  border-left: 4px solid #409eff;
  margin-bottom: 16px;
  border-radius: 4px;
}

.result-answer {
  padding: 16px;
  background: #f5f7fa;
  border-radius: 8px;
  color: #606266;
  white-space: pre-wrap;
}
</style>



