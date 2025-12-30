<template>
  <div class="page-container">
    <div class="page-header">
      <h1><el-icon><Folder /></el-icon> RAG知识库</h1>
      <p>检索增强生成 - 文档检索与智能问答</p>
    </div>

    <el-row :gutter="20">
      <!-- 左侧：知识库管理 -->
      <el-col :span="10">
        <el-card class="demo-card">
          <template #header>
            <div class="card-header">
              <span>📚 知识库管理</span>
            </div>
          </template>

          <el-form :model="docForm" label-width="100px">
            <el-form-item label="知识库ID">
              <el-input v-model="docForm.knowledgeBaseId" placeholder="例如：tech_docs" />
            </el-form-item>

            <el-form-item label="文档标题">
              <el-input v-model="docForm.title" placeholder="文档标题" />
            </el-form-item>

            <el-form-item label="文档内容">
              <el-input
                v-model="docForm.content"
                type="textarea"
                :rows="8"
                placeholder="粘贴文档内容..."
              />
            </el-form-item>

            <el-form-item>
              <el-button type="primary" @click="addDocument" :loading="addLoading">
                <el-icon><Upload /></el-icon> 添加文档
              </el-button>
            </el-form-item>
          </el-form>

          <div class="knowledge-bases">
            <h4>现有知识库</h4>
            <el-tag
              v-for="kb in knowledgeBases"
              :key="kb.id"
              style="margin-right: 8px; margin-bottom: 8px;"
            >
              {{ kb.name }} ({{ kb.chunkCount }}块)
            </el-tag>
          </div>
        </el-card>
      </el-col>

      <!-- 右侧：问答 -->
      <el-col :span="14">
        <el-card class="demo-card">
          <template #header>
            <div class="card-header">
              <span>💬 智能问答</span>
            </div>
          </template>

          <el-form :model="queryForm" label-width="100px">
            <el-form-item label="知识库">
              <el-input v-model="queryForm.knowledgeBaseId" placeholder="知识库ID" />
            </el-form-item>

            <el-form-item label="问题">
              <el-input
                v-model="queryForm.question"
                type="textarea"
                :rows="4"
                placeholder="输入问题..."
              />
            </el-form-item>

            <el-form-item>
              <el-button type="primary" @click="query" :loading="queryLoading">
                <el-icon><Search /></el-icon> 提问
              </el-button>
            </el-form-item>
          </el-form>

          <div v-if="answer" class="answer-section">
            <h4>💡 答案</h4>
            <div class="answer-content">{{ answer }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import axios from 'axios'

const api = axios.create({ baseURL: '/api' })

const docForm = ref({
  knowledgeBaseId: 'default',
  title: '',
  content: ''
})

const queryForm = ref({
  knowledgeBaseId: 'default',
  question: ''
})

const addLoading = ref(false)
const queryLoading = ref(false)
const knowledgeBases = ref([])
const answer = ref('')

onMounted(() => {
  loadKnowledgeBases()
})

const addDocument = async () => {
  if (!docForm.value.title || !docForm.value.content) {
    ElMessage.warning('请填写完整信息')
    return
  }

  addLoading.value = true
  try {
    const res = await api.post('/rag/document/add', docForm.value)
    if (res.data.success) {
      ElMessage.success('文档添加成功')
      docForm.value.title = ''
      docForm.value.content = ''
      loadKnowledgeBases()
    }
  } catch (error) {
    ElMessage.error('添加失败')
  } finally {
    addLoading.value = false
  }
}

const query = async () => {
  if (!queryForm.value.question) {
    ElMessage.warning('请输入问题')
    return
  }

  queryLoading.value = true
  answer.value = ''

  try {
    const res = await api.post('/rag/query', queryForm.value)
    if (res.data.success) {
      answer.value = res.data.answer
    }
  } catch (error) {
    ElMessage.error('查询失败')
  } finally {
    queryLoading.value = false
  }
}

const loadKnowledgeBases = async () => {
  try {
    const res = await api.get('/rag/knowledge-bases')
    if (res.data.success) {
      knowledgeBases.value = res.data.data
    }
  } catch (error) {
    console.error('加载知识库失败', error)
  }
}
</script>

<style scoped>
.page-container {
  padding: 40px;
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
}

.demo-card {
  background: rgba(138, 43, 226, 0.05);
  border: 1px solid rgba(138, 43, 226, 0.2);
}

.card-header {
  font-weight: bold;
  font-size: 16px;
}

.knowledge-bases {
  margin-top: 24px;
  padding-top: 16px;
  border-top: 1px solid rgba(138, 43, 226, 0.2);
}

.knowledge-bases h4 {
  color: #ba55d3;
  margin-bottom: 12px;
}

.answer-section {
  margin-top: 24px;
  padding-top: 16px;
  border-top: 1px solid rgba(138, 43, 226, 0.2);
}

.answer-section h4 {
  color: #ba55d3;
  margin-bottom: 12px;
}

.answer-content {
  background: rgba(0, 0, 0, 0.3);
  padding: 16px;
  border-radius: 8px;
  color: #fff;
  line-height: 1.6;
  white-space: pre-wrap;
}
</style>



