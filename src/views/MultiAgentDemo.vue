<template>
  <div class="page-container">
    <div class="page-header">
      <h1><el-icon><Connection /></el-icon> Multi-Agent协作</h1>
      <p>多Agent系统 - 监督者模式、并行执行、Agent间通信</p>
    </div>

    <el-tabs v-model="activeTab">
      <!-- 监督者模式 -->
      <el-tab-pane label="监督者模式" name="supervisor">
        <div class="tab-content">
          <p class="description">
            🎯 一个主Agent协调多个专家Agent共同完成复杂任务
          </p>

          <el-input
            v-model="supervisorTask"
            type="textarea"
            :rows="4"
            placeholder="输入需要多专家协作的任务，例如：设计一个电商系统的完整方案"
          />

          <el-button 
            type="primary" 
            @click="runSupervisor"
            :loading="supervisorLoading"
            size="large"
          >
            <el-icon><CaretRight /></el-icon> 执行监督者模式
          </el-button>

          <div v-if="supervisorResult" class="result-section">
            <h3>执行结果</h3>

            <el-card class="result-card">
              <h4>📋 任务分配计划</h4>
              <pre>{{ supervisorResult.plan }}</pre>
            </el-card>

            <div v-if="supervisorResult.expertResults">
              <h4>👥 专家执行结果 ({{ supervisorResult.expertCount }}位专家)</h4>
              <el-card 
                v-for="(expert, index) in supervisorResult.expertResults" 
                :key="index"
                class="expert-card"
              >
                <h5>{{ expert.expertName }}</h5>
                <div class="expert-task">任务: {{ expert.task }}</div>
                <div class="expert-result">{{ expert.result }}</div>
              </el-card>
            </div>

            <el-card class="result-card final">
              <h4>🎉 最终综合报告</h4>
              <pre>{{ supervisorResult.finalResult }}</pre>
            </el-card>
          </div>
        </div>
      </el-tab-pane>

      <!-- 并行执行 -->
      <el-tab-pane label="并行执行" name="parallel">
        <div class="tab-content">
          <p class="description">
            ⚡ 多个Agent同时处理不同任务，提高效率
          </p>

          <div v-for="(task, index) in parallelTasks" :key="index" class="task-input">
            <el-input
              v-model="parallelTasks[index]"
              placeholder="输入任务..."
            >
              <template #append>
                <el-button @click="removeTask(index)" text>
                  <el-icon><Close /></el-icon>
                </el-button>
              </template>
            </el-input>
          </div>

          <el-button @click="addTask" text style="margin-bottom: 16px;">
            <el-icon><Plus /></el-icon> 添加任务
          </el-button>

          <el-button 
            type="primary" 
            @click="runParallel"
            :loading="parallelLoading"
            size="large"
          >
            <el-icon><CaretRight /></el-icon> 并行执行
          </el-button>

          <div v-if="parallelResult" class="result-section">
            <h3>执行结果 ({{ parallelResult.taskCount }}个任务)</h3>

            <el-card 
              v-for="(result, index) in parallelResult.results" 
              :key="index"
              class="parallel-card"
            >
              <div class="parallel-header">
                <span class="agent-name">{{ result.agentName }}</span>
                <el-tag :type="result.status === 'success' ? 'success' : 'danger'">
                  {{ result.status }}
                </el-tag>
                <span class="duration">{{ result.duration }}ms</span>
              </div>
              <div class="parallel-task">任务: {{ result.task }}</div>
              <div class="parallel-result">{{ result.result }}</div>
            </el-card>
          </div>
        </div>
      </el-tab-pane>

      <!-- Agent间通信 -->
      <el-tab-pane label="Agent间通信" name="communication">
        <div class="tab-content">
          <p class="description">
            📡 Agent之间传递消息，逐步深化处理
          </p>

          <el-input
            v-model="communicationMessage"
            type="textarea"
            :rows="4"
            placeholder="输入初始消息，将由多个Agent接力处理..."
          />

          <el-button 
            type="primary" 
            @click="runCommunication"
            :loading="communicationLoading"
            size="large"
          >
            <el-icon><CaretRight /></el-icon> 开始通信
          </el-button>

          <div v-if="communicationResult" class="result-section">
            <h3>通信流程</h3>

            <div class="communication-flow">
              <div 
                v-for="(step, index) in communicationResult.steps" 
                :key="index"
                class="communication-step"
              >
                <div class="step-header">
                  <el-icon><User /></el-icon>
                  <span>{{ step.fromAgent }}</span>
                  <el-icon><Right /></el-icon>
                  <span>{{ step.toAgent }}</span>
                </div>
                <div class="step-message">
                  <strong>消息:</strong> {{ step.message }}
                </div>
                <div class="step-response">
                  <strong>响应:</strong> {{ step.response }}
                </div>
              </div>
            </div>

            <el-card class="result-card final">
              <h4>🎯 最终结果</h4>
              <pre>{{ communicationResult.finalResult }}</pre>
            </el-card>
          </div>
        </div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import axios from 'axios'

const api = axios.create({ baseURL: '/api' })
const activeTab = ref('supervisor')

// 监督者模式
const supervisorTask = ref('')
const supervisorLoading = ref(false)
const supervisorResult = ref(null)

const runSupervisor = async () => {
  if (!supervisorTask.value) return

  supervisorLoading.value = true
  supervisorResult.value = null

  try {
    const res = await api.post('/multi-agent/supervisor', { task: supervisorTask.value })
    supervisorResult.value = res.data
    ElMessage.success('执行完成')
  } catch (error) {
    ElMessage.error('执行失败')
  } finally {
    supervisorLoading.value = false
  }
}

// 并行执行
const parallelTasks = ref(['', '', ''])
const parallelLoading = ref(false)
const parallelResult = ref(null)

const addTask = () => {
  parallelTasks.value.push('')
}

const removeTask = (index) => {
  if (parallelTasks.value.length > 1) {
    parallelTasks.value.splice(index, 1)
  }
}

const runParallel = async () => {
  const tasks = parallelTasks.value.filter(t => t.trim())
  if (tasks.length === 0) {
    ElMessage.warning('请至少输入一个任务')
    return
  }

  parallelLoading.value = true
  parallelResult.value = null

  try {
    const res = await api.post('/multi-agent/parallel', { tasks })
    parallelResult.value = res.data
    ElMessage.success('执行完成')
  } catch (error) {
    ElMessage.error('执行失败')
  } finally {
    parallelLoading.value = false
  }
}

// Agent间通信
const communicationMessage = ref('')
const communicationLoading = ref(false)
const communicationResult = ref(null)

const runCommunication = async () => {
  if (!communicationMessage.value) return

  communicationLoading.value = true
  communicationResult.value = null

  try {
    const res = await api.post('/multi-agent/communication', { message: communicationMessage.value })
    communicationResult.value = res.data
    ElMessage.success('通信完成')
  } catch (error) {
    ElMessage.error('通信失败')
  } finally {
    communicationLoading.value = false
  }
}
</script>

<style scoped>
.page-container {
  padding: 40px;
  max-width: 1400px;
  margin: 0 auto;
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
  margin-bottom: 24px;
}

.tab-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding: 20px 0;
}

.description {
  color: rgba(255, 255, 255, 0.8);
  font-size: 14px;
  padding: 12px;
  background: rgba(138, 43, 226, 0.1);
  border-radius: 6px;
}

.result-section {
  margin-top: 32px;
  padding-top: 32px;
  border-top: 1px solid rgba(138, 43, 226, 0.2);
}

.result-section h3, .result-section h4 {
  color: #fff;
  margin-bottom: 16px;
}

.result-card {
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid rgba(138, 43, 226, 0.2);
  padding: 16px;
  margin-bottom: 16px;
}

.result-card h4 {
  color: #ba55d3;
  margin-bottom: 12px;
}

.result-card pre {
  white-space: pre-wrap;
  color: rgba(255, 255, 255, 0.9);
}

.result-card.final {
  border-color: rgba(186, 85, 211, 0.5);
}

.expert-card {
  background: rgba(138, 43, 226, 0.05);
  padding: 12px;
  margin-bottom: 12px;
}

.expert-card h5 {
  color: #ba55d3;
  margin-bottom: 8px;
}

.expert-task {
  color: rgba(255, 255, 255, 0.6);
  font-size: 13px;
  margin-bottom: 8px;
}

.expert-result {
  color: #fff;
  line-height: 1.5;
}

.task-input {
  margin-bottom: 12px;
}

.parallel-card {
  background: rgba(138, 43, 226, 0.05);
  padding: 12px;
  margin-bottom: 12px;
}

.parallel-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}

.agent-name {
  color: #ba55d3;
  font-weight: bold;
}

.duration {
  color: rgba(255, 255, 255, 0.5);
  font-size: 12px;
}

.parallel-task {
  color: rgba(255, 255, 255, 0.6);
  font-size: 13px;
  margin-bottom: 8px;
}

.parallel-result {
  color: #fff;
}

.communication-flow {
  margin-bottom: 24px;
}

.communication-step {
  background: rgba(138, 43, 226, 0.05);
  padding: 16px;
  margin-bottom: 16px;
  border-radius: 8px;
  border-left: 3px solid #ba55d3;
}

.step-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
  color: #ba55d3;
  font-weight: bold;
}

.step-message, .step-response {
  color: rgba(255, 255, 255, 0.9);
  margin-bottom: 8px;
  line-height: 1.5;
}

.step-message strong, .step-response strong {
  color: #ba55d3;
}
</style>



