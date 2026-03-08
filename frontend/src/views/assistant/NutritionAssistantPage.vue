<template>
  <div class="page-shell assistant-page">
    <section class="page-header">
      <div>
        <div class="page-eyebrow">营养助手</div>
        <h2 class="page-title">让 AI 结合档案、饮食记录和评估做辅助总结</h2>
        <p class="page-subtitle">
          这里不是闲聊机器人，而是会读取健康档案、最近饮食和评估结果的营养辅助助手。
        </p>
      </div>
    </section>

    <section class="assistant-grid">
      <div class="page-card assistant-chat">
        <div class="assistant-panel-title">对话区</div>
        <div class="chat-list">
          <div v-for="(message, index) in messages" :key="index" class="chat-item" :class="`chat-item--${message.role}`">
            <div class="chat-item__role">{{ message.role === 'assistant' ? '营养助手' : '我' }}</div>
            <div class="chat-item__content">{{ message.content }}</div>
          </div>
        </div>
        <el-input
          v-model="draft"
          type="textarea"
          :rows="4"
          resize="none"
          placeholder="例如：我最近晚饭后总想吃零食，怎样调整更合理？"
        />
        <div class="assistant-actions">
          <el-button class="soft-button" :loading="sending" @click="handleSend">发送问题</el-button>
        </div>
      </div>

      <div class="page-card assistant-side">
        <div class="assistant-panel-title">上下文来源</div>
        <div class="context-switches">
          <el-checkbox v-model="options.includeProfile">附带健康档案</el-checkbox>
          <el-checkbox v-model="options.includeRecentMeals">附带最近三餐</el-checkbox>
          <el-checkbox v-model="options.includeLatestAssessment">附带最近一次评估</el-checkbox>
        </div>

        <div class="assistant-note-list">
          <div class="assistant-note">
            <strong>当前会一起发送</strong>
            <span>{{ selectedSourceSummary }}</span>
          </div>
          <div class="assistant-note">
            <strong>AI 配置方式</strong>
            <span>当前后端通过环境变量注入 AI 开关、渠道、Base URL、密钥和模型名。</span>
          </div>
          <div class="assistant-note">
            <strong>当前边界</strong>
            <span>不做医疗诊断；当 AI 不可用时会自动降级为规则化建议。</span>
          </div>
        </div>

        <div v-if="lastSources.length" class="source-tags">
          <span v-for="source in lastSources" :key="source">{{ source }}</span>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { nutritionChatApi } from '@/api/assistant'

type ChatMessage = {
  role: 'assistant' | 'user'
  content: string
}

const draft = ref('')
const sending = ref(false)
const lastSources = ref<string[]>([])
const options = reactive({
  includeProfile: true,
  includeRecentMeals: true,
  includeLatestAssessment: true
})

const messages = ref<ChatMessage[]>([
  {
    role: 'assistant',
    content: '你好，我会结合你的健康档案、近期饮食和评估结果，给出更贴近场景的营养建议。'
  }
])

const selectedSourceSummary = computed(() => {
  const sources = []
  if (options.includeProfile) sources.push('健康档案')
  if (options.includeRecentMeals) sources.push('最近三餐')
  if (options.includeLatestAssessment) sources.push('最近一次评估')
  return sources.length ? sources.join('、') : '仅发送当前提问'
})

const handleSend = async () => {
  const message = draft.value.trim()
  if (!message) {
    ElMessage.warning('请输入你想咨询的问题')
    return
  }

  messages.value.push({ role: 'user', content: message })
  draft.value = ''
  sending.value = true

  try {
    const response = await nutritionChatApi({
      message,
      includeProfile: options.includeProfile,
      includeRecentMeals: options.includeRecentMeals,
      includeLatestAssessment: options.includeLatestAssessment
    })
    messages.value.push({ role: 'assistant', content: response.reply })
    lastSources.value = response.sources || []
    if (!response.usedAi) {
      ElMessage.info('当前使用的是规则化建议。若要启用 AI，请配置后端 AI 环境变量。')
    }
  } finally {
    sending.value = false
  }
}
</script>

<style scoped>
.assistant-page {
  gap: 24px;
}

.assistant-grid {
  display: grid;
  grid-template-columns: 1.35fr 0.85fr;
  gap: 20px;
}

.assistant-chat,
.assistant-side {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.assistant-panel-title {
  font-size: 14px;
  letter-spacing: 0.16em;
  text-transform: uppercase;
  color: var(--color-primary-deep);
}

.chat-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
  min-height: 360px;
}

.chat-item {
  max-width: 88%;
  padding: 16px 18px;
  border-radius: 22px;
  line-height: 1.8;
}

.chat-item--assistant {
  align-self: flex-start;
  background: rgba(255, 255, 255, 0.78);
}

.chat-item--user {
  align-self: flex-end;
  background: linear-gradient(135deg, rgba(125, 164, 130, 0.16), rgba(215, 185, 141, 0.2));
}

.chat-item__role {
  margin-bottom: 8px;
  font-size: 12px;
  color: var(--color-text-sub);
}

.assistant-actions {
  display: flex;
  justify-content: flex-end;
}

.context-switches {
  display: grid;
  gap: 12px;
  padding: 16px;
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.72);
}

.assistant-note-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.assistant-note {
  padding: 16px;
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.72);
  display: flex;
  flex-direction: column;
  gap: 8px;
  line-height: 1.7;
}

.source-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.source-tags span {
  padding: 8px 12px;
  border-radius: 999px;
  background: rgba(125, 164, 130, 0.12);
  color: var(--color-primary-deep);
  font-size: 12px;
}

@media (max-width: 1100px) {
  .assistant-grid {
    grid-template-columns: 1fr;
  }
}
</style>
