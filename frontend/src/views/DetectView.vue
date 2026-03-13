<template>
  <div class="detect-grid">
    <section class="panel">
      <div class="section-title">
        <div>
          <h2>图像检测</h2>
          <div class="subtle">上传待检图片并设置推理参数</div>
        </div>
        <div v-if="result" class="status-pill" :class="result.data.has_anomaly ? 'danger' : 'ok'">
          {{ result.data.has_anomaly ? '检测到异常' : '未检测到异常' }}
        </div>
      </div>

      <el-form label-width="120px">
        <div class="upload-grid">
          <el-form-item label="待检图片">
            <div class="upload-card" @click="triggerImage">
              <input ref="imageInput" type="file" accept="image/*" class="hidden-input" @change="onImageChange" />
              <div v-if="preview" class="upload-preview">
                <img :src="preview" alt="待检图片" />
              </div>
              <div v-else class="upload-placeholder">点击上传待检图片</div>
            </div>
          </el-form-item>
          <el-form-item label="正常参考图">
            <div class="upload-card" @click="triggerNormal">
              <input ref="normalInput" type="file" accept="image/*" class="hidden-input" @change="onNormalChange" />
              <div v-if="normalPreview" class="upload-preview">
                <img :src="normalPreview" alt="参考图片" />
              </div>
              <div v-else class="upload-placeholder">点击上传正常参考图</div>
            </div>
          </el-form-item>
        </div>

        <div class="param-grid">
          <el-form-item label="提示词">
            <el-input v-model="prompt" placeholder="Describe this image." />
          </el-form-item>
          <el-form-item label="max_tgt_len">
            <el-input-number v-model="maxTgtLen" :min="1" :max="512" />
          </el-form-item>
          <el-form-item label="top_p">
            <el-input-number v-model="topP" :min="0" :max="1" :step="0.01" />
          </el-form-item>
          <el-form-item label="temperature">
            <el-input-number v-model="temperature" :min="0" :max="2" :step="0.1" />
          </el-form-item>
        </div>

        <el-button type="primary" :loading="loadingDetect" @click="onDetect">开始检测</el-button>
      </el-form>

      <div class="result-block">
        <h3>检测结果</h3>
        <div v-if="result">
          <div class="result-meta">
            <div class="meta-card">
              <div class="subtle">描述</div>
              <div class="meta-value">{{ result.data.description }}</div>
            </div>
            <div class="meta-card">
              <div class="subtle">异常状态</div>
              <div class="meta-value">
                <span class="status-pill" :class="result.data.has_anomaly ? 'danger' : 'ok'">
                  {{ result.data.has_anomaly ? '异常' : '正常' }}
                </span>
              </div>
            </div>
          </div>
          <div class="image-grid">
            <div class="image-box">
              <div class="subtle">缺陷位置图</div>
              <div class="image-frame">
                <img v-if="heatmap" :src="heatmap" alt="缺陷位置图" />
                <div v-else class="image-empty">暂无缺陷位置图</div>
              </div>
            </div>
          </div>
        </div>
        <div v-else class="subtle">暂无检测结果</div>
      </div>
    </section>

    <section class="panel chat-panel">
      <div class="chat-header">
        <div>
          <h2>检测对话</h2>
          <div class="subtle">基于当前图像的多轮问答</div>
        </div>
        <div class="chat-actions">
          <el-button size="small" plain class="icon-btn" @click="openHistory">
            <span class="icon-clock" aria-hidden="true">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
                <circle cx="12" cy="12" r="9" />
                <path d="M12 7v5l3 2" />
              </svg>
            </span>
            历史
          </el-button>
          <el-button size="small" plain @click="clearChat">清空对话</el-button>
        </div>
      </div>

      <el-drawer v-model="historyDrawer" title="历史对话" size="360px">
        <div class="history-list">
          <div
            v-for="session in historyOptions"
            :key="session.id"
            class="history-item"
            :class="{ active: session.id === activeSessionId }"
          >
            <div class="history-main" @click="onSelectHistory(session.id)">
              <div class="history-title">{{ session.title || '未命名对话' }}</div>
              <div class="subtle">{{ session.updatedAt }}</div>
            </div>
            <el-dropdown trigger="click">
              <span class="history-more" @click.stop>
                <svg viewBox="0 0 24 24" fill="currentColor">
                  <circle cx="12" cy="5" r="1.8" />
                  <circle cx="12" cy="12" r="1.8" />
                  <circle cx="12" cy="19" r="1.8" />
                </svg>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item @click="openRename(session)">重命名</el-dropdown-item>
                  <el-dropdown-item divided @click="deleteSessionById(session.id)">删除</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>
        <div class="history-actions">
          <el-button type="primary" @click="createNewSession">新建对话</el-button>
        </div>
      </el-drawer>

      <el-dialog v-model="renameDialog" title="重命名对话" width="360px">
        <el-input v-model="renameTitle" placeholder="输入对话名称" />
        <template #footer>
          <el-button @click="renameDialog = false">取消</el-button>
          <el-button type="primary" @click="confirmRename">保存</el-button>
        </template>
      </el-dialog>

      <div class="chat-body" ref="chatBody">
        <div v-for="(msg, idx) in messages" :key="idx" :class="['chat-item', msg.role]">
          <div class="chat-avatar">{{ msg.role === 'user' ? '你' : 'AI' }}</div>
          <div class="chat-bubble">
            <div class="chat-text">{{ msg.content }}</div>
            <div class="subtle">{{ msg.time }}</div>
          </div>
        </div>
      </div>

      <div class="chat-input">
        <el-input
          v-model="chatInput"
          placeholder="输入问题，例如：缺陷是什么？可能原因？"
          @keyup.enter="onAsk"
          :disabled="loadingChat"
        />
        <el-button type="primary" :loading="loadingChat" @click="onAsk">发送</el-button>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { api } from '../api'
import type {
  ChatMessage,
  ChatSession,
  ChatSessionSummary,
  InferenceDetectResponse
} from '../api/types'
import { useAuthStore } from '../stores/auth'

type ChatRole = 'user' | 'assistant'

const defaultAssistantMessage = (): ChatMessage => ({
  role: 'assistant',
  content: '请先上传图片并执行检测，我会基于图像回答问题。',
  time: new Date().toLocaleTimeString()
})

const auth = useAuthStore()
const userId = computed(() => auth.user?.id || null)

const imageFile = ref<File | null>(null)
const normalFile = ref<File | null>(null)
const prompt = ref('Describe this image.')
const maxTgtLen = ref(128)
const topP = ref(0.01)
const temperature = ref(1)

const loadingDetect = ref(false)
const loadingChat = ref(false)

const result = ref<InferenceDetectResponse | null>(null)
const preview = ref<string | null>(null)
const normalPreview = ref<string | null>(null)
const heatmap = ref<string | null>(null)

const imageDataUrl = ref<string | null>(null)
const normalDataUrl = ref<string | null>(null)

const imageInput = ref<HTMLInputElement | null>(null)
const normalInput = ref<HTMLInputElement | null>(null)

const chatInput = ref('')
const chatBody = ref<HTMLElement | null>(null)
const messages = ref<ChatMessage[]>([defaultAssistantMessage()])

const sessions = ref<ChatSessionSummary[]>([])
const activeSessionId = ref<number | null>(null)

const historyDrawer = ref(false)
const renameDialog = ref(false)
const renameTargetId = ref<number | null>(null)
const renameTitle = ref('')

const historyOptions = computed(() => {
  return [...sessions.value].sort((a, b) => b.updatedAt.localeCompare(a.updatedAt))
})

onMounted(async () => {
  await loadHistory()
})

const openHistory = async () => {
  historyDrawer.value = true
  await refreshHistoryList()
}

const triggerImage = () => imageInput.value?.click()
const triggerNormal = () => normalInput.value?.click()

const onImageChange = (e: Event) => {
  const file = (e.target as HTMLInputElement).files?.[0] || null
  imageFile.value = file
  preview.value = file ? URL.createObjectURL(file) : null
  if (file) {
    readAsDataUrl(file).then((url) => {
      imageDataUrl.value = url
      syncSession()
    })
  }
}

const onNormalChange = (e: Event) => {
  const file = (e.target as HTMLInputElement).files?.[0] || null
  normalFile.value = file
  normalPreview.value = file ? URL.createObjectURL(file) : null
  if (file) {
    readAsDataUrl(file).then((url) => {
      normalDataUrl.value = url
      syncSession()
    })
  }
}

const callDetect = async (customPrompt?: string) => {
  ensureFilesFromDataUrl()
  if (!imageFile.value) {
    ElMessage.warning('请先选择待检图片')
    return null
  }
  const form = new FormData()
  form.append('image', imageFile.value)
  if (normalFile.value) form.append('normal_image', normalFile.value)
  form.append('prompt', customPrompt || prompt.value)
  form.append('max_tgt_len', String(maxTgtLen.value))
  form.append('top_p', String(topP.value))
  form.append('temperature', String(temperature.value))
  const res = await api.detect(form)
  if (!res.success) {
    throw new Error(res.message || 'detect_failed')
  }
  return res.data
}

const onDetect = async () => {
  loadingDetect.value = true
  try {
    const data = await callDetect()
    if (!data) return
    result.value = data
    heatmap.value = `data:image/png;base64,${data.data.localization_image_base64}`
    const answer = data.data.description
    messages.value.push({
      role: 'assistant',
      content: answer,
      time: new Date().toLocaleTimeString()
    })
    await syncSession()
    await scrollChat()
  } catch (err: any) {
    ElMessage.error(err?.message || '检测失败')
  } finally {
    loadingDetect.value = false
  }
}

const onAsk = async () => {
  if (!chatInput.value.trim()) return
  ensureFilesFromDataUrl()
  if (!imageFile.value) {
    ElMessage.warning('请先上传图片并完成检测')
    return
  }
  const question = chatInput.value.trim()
  messages.value.push({ role: 'user', content: question, time: new Date().toLocaleTimeString() })
  chatInput.value = ''
  loadingChat.value = true
  await scrollChat()
  try {
    const data = await callDetect(question)
    if (!data) return
    result.value = data
    heatmap.value = `data:image/png;base64,${data.data.localization_image_base64}`
    const answer = data.data.description
    messages.value.push({
      role: 'assistant',
      content: answer,
      time: new Date().toLocaleTimeString()
    })
    await syncSession(question)
  } catch (err: any) {
    ElMessage.error(err?.message || '对话失败')
  } finally {
    loadingChat.value = false
    await scrollChat()
  }
}

const clearChat = async () => {
  messages.value = [defaultAssistantMessage()]
  await syncSession()
}

const onSelectHistory = async (id: number) => {
  const res = await api.chatGet(id)
  if (!res.success) {
    ElMessage.error(res.message || '加载历史失败')
    return
  }
  applySession(res.data)
  historyDrawer.value = false
}

const createNewSession = async () => {
  if (!userId.value) return
  resetSessionState()
  const payload = buildPayload('新对话')
  const res = await api.chatCreate(payload)
  if (!res.success) {
    ElMessage.error(res.message || '创建失败')
    return
  }
  await loadHistory(res.data.id)
}

const deleteSessionById = async (id: number) => {
  const res = await api.chatDelete(id)
  if (!res.success) {
    ElMessage.error(res.message || '删除失败')
    return
  }
  if (id === activeSessionId.value) {
    await loadHistory()
  } else {
    await refreshHistoryList()
  }
}

const loadHistory = async (preferId?: number) => {
  if (!userId.value) return
  const res = await api.chatList(userId.value)
  if (!res.success) {
    ElMessage.error(res.message || '加载历史失败')
    return
  }
  sessions.value = res.data
  const targetId = preferId || res.data[0]?.id
  if (targetId) {
    activeSessionId.value = targetId
    const detail = await api.chatGet(targetId)
    if (detail.success) {
      applySession(detail.data)
    }
  } else {
    activeSessionId.value = null
    messages.value = [defaultAssistantMessage()]
  }
}

const refreshHistoryList = async () => {
  if (!userId.value) return
  const res = await api.chatList(userId.value)
  if (res.success) {
    sessions.value = res.data
  }
}

const openRename = (session: ChatSessionSummary) => {
  renameTargetId.value = session.id
  renameTitle.value = session.title || ''
  renameDialog.value = true
}

const confirmRename = async () => {
  if (!renameTargetId.value) return
  const title = renameTitle.value.trim()
  if (!title) {
    ElMessage.warning('请输入对话名称')
    return
  }
  const detail = await api.chatGet(renameTargetId.value)
  if (!detail.success) {
    ElMessage.error(detail.message || '加载失败')
    return
  }
  const payload = buildPayloadFromSession(detail.data, title)
  const res = await api.chatUpdate(renameTargetId.value, payload)
  if (!res.success) {
    ElMessage.error(res.message || '保存失败')
    return
  }
  renameDialog.value = false
  await refreshHistoryList()
  if (renameTargetId.value === activeSessionId.value) {
    await loadHistory(activeSessionId.value)
  }
}

const syncSession = async (latestUserQuestion?: string) => {
  if (!userId.value) return
  const payload = buildPayload()
  if (payload.title === '新对话' && latestUserQuestion) {
    payload.title = latestUserQuestion.slice(0, 16)
  }
  if (!activeSessionId.value) {
    const created = await api.chatCreate(payload)
    if (created.success) {
      activeSessionId.value = created.data.id
      await loadHistory(created.data.id)
    }
    return
  }
  const res = await api.chatUpdate(activeSessionId.value, payload)
  if (!res.success) {
    ElMessage.error(res.message || '保存失败')
  } else {
    await loadHistory(activeSessionId.value)
  }
}

const buildPayload = (title?: string): Partial<ChatSession> => {
  return {
    userId: userId.value || undefined,
    title: title || sessions.value.find((s) => s.id === activeSessionId.value)?.title || '新对话',
    prompt: prompt.value,
    maxTgtLen: maxTgtLen.value,
    topP: topP.value,
    temperature: temperature.value,
    imageDataUrl: imageDataUrl.value || undefined,
    normalDataUrl: normalDataUrl.value || undefined,
    result: result.value || undefined,
    messages: [...messages.value]
  }
}

const buildPayloadFromSession = (session: ChatSession, title: string): Partial<ChatSession> => {
  return {
    userId: session.userId,
    title,
    prompt: session.prompt,
    maxTgtLen: session.maxTgtLen,
    topP: session.topP,
    temperature: session.temperature,
    imageDataUrl: session.imageDataUrl || undefined,
    normalDataUrl: session.normalDataUrl || undefined,
    result: session.result || undefined,
    messages: session.messages || []
  }
}

const applySession = (session: ChatSession) => {
  messages.value = session.messages?.length ? [...session.messages] : [defaultAssistantMessage()]
  prompt.value = session.prompt || prompt.value
  maxTgtLen.value = session.maxTgtLen || 128
  topP.value = session.topP ?? 0.01
  temperature.value = session.temperature ?? 1
  imageDataUrl.value = session.imageDataUrl || null
  normalDataUrl.value = session.normalDataUrl || null
  result.value = (session.result as InferenceDetectResponse) || null
  preview.value = imageDataUrl.value
  normalPreview.value = normalDataUrl.value
  heatmap.value = result.value?.data?.localization_image_base64
    ? `data:image/png;base64,${result.value.data.localization_image_base64}`
    : null
  ensureFilesFromDataUrl()
  nextTick().then(scrollChat)
}

const resetSessionState = () => {
  messages.value = [defaultAssistantMessage()]
  result.value = null
  heatmap.value = null
  imageFile.value = null
  normalFile.value = null
  preview.value = null
  normalPreview.value = null
  imageDataUrl.value = null
  normalDataUrl.value = null
  chatInput.value = ''
}

const scrollChat = async () => {
  await nextTick()
  if (chatBody.value) {
    chatBody.value.scrollTop = chatBody.value.scrollHeight
  }
}

const readAsDataUrl = (file: File): Promise<string> => {
  return new Promise((resolve, reject) => {
    const reader = new FileReader()
    reader.onload = () => resolve(String(reader.result))
    reader.onerror = () => reject(reader.error)
    reader.readAsDataURL(file)
  })
}

const ensureFilesFromDataUrl = () => {
  if (!imageFile.value && imageDataUrl.value) {
    imageFile.value = dataUrlToFile(imageDataUrl.value, 'history-image.png')
  }
  if (!normalFile.value && normalDataUrl.value) {
    normalFile.value = dataUrlToFile(normalDataUrl.value, 'history-normal.png')
  }
}

const dataUrlToFile = (dataUrl: string, filename: string): File => {
  const [header, data] = dataUrl.split(',')
  const mime = header.match(/:(.*?);/)?.[1] || 'image/png'
  const binary = atob(data)
  const array = new Uint8Array(binary.length)
  for (let i = 0; i < binary.length; i += 1) {
    array[i] = binary.charCodeAt(i)
  }
  return new File([array], filename, { type: mime })
}
</script>

<style scoped>
.detect-grid {
  display: grid;
  grid-template-columns: minmax(360px, 1fr) minmax(360px, 1fr);
  gap: 20px;
}

.section-title {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.section-title h2,
.section-title h3 {
  margin: 0;
}

.status-pill {
  padding: 6px 14px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 600;
  background: #e2e8f0;
  color: #334155;
}

.status-pill.ok {
  background: rgba(14, 165, 164, 0.15);
  color: #0f766e;
}

.status-pill.danger {
  background: rgba(239, 68, 68, 0.12);
  color: #b91c1c;
}

.hidden-input {
  display: none;
}

.upload-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 16px;
}

.upload-card {
  border: 1px dashed #cbd5f5;
  border-radius: 14px;
  background: #ffffff;
  padding: 12px;
  cursor: pointer;
  min-height: 180px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: border-color 0.2s ease, box-shadow 0.2s ease;
}

.upload-card:hover {
  border-color: #38bdf8;
  box-shadow: 0 6px 16px rgba(56, 189, 248, 0.18);
}

.upload-preview img {
  max-width: 100%;
  max-height: 220px;
  border-radius: 10px;
}

.upload-placeholder {
  color: #64748b;
}

.param-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 8px 16px;
  margin-bottom: 12px;
}

.result-block {
  margin-top: 24px;
}

.result-block h3 {
  margin: 0 0 12px 0;
}

.result-meta {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 12px;
  margin-bottom: 16px;
}

.meta-card {
  background: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  padding: 12px;
}

.meta-value {
  margin-top: 6px;
  font-weight: 600;
  color: #0f172a;
}

.image-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(260px, 1fr));
  gap: 12px;
}

.image-box {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.image-frame {
  border-radius: 12px;
  border: 1px solid #e2e8f0;
  background: #ffffff;
  min-height: 240px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 8px;
}

.image-frame img {
  max-width: 100%;
  border-radius: 10px;
}

.image-empty {
  color: #94a3b8;
}

.chat-panel {
  display: flex;
  flex-direction: column;
  min-height: 620px;
}

.chat-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.chat-header h2 {
  margin: 0;
}

.chat-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.icon-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.icon-clock svg {
  width: 16px;
  height: 16px;
}

.history-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.history-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 12px;
  border-radius: 12px;
  border: 1px solid #e2e8f0;
  background: #fff;
  cursor: pointer;
}

.history-item.active {
  border-color: #38bdf8;
  box-shadow: 0 4px 12px rgba(56, 189, 248, 0.18);
}

.history-main {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.history-title {
  font-weight: 600;
  color: #0f172a;
}

.history-more {
  width: 24px;
  height: 24px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  color: #64748b;
}

.history-more svg {
  width: 16px;
  height: 16px;
}

.history-actions {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

.chat-body {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  background: linear-gradient(180deg, #ffffff 0%, #f8fafc 100%);
}

.chat-item {
  display: flex;
  gap: 12px;
  margin-bottom: 12px;
}

.chat-item.user {
  flex-direction: row-reverse;
}

.chat-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: #0ea5a4;
  color: #fff;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  flex-shrink: 0;
}

.chat-item.user .chat-avatar {
  background: #64748b;
}

.chat-bubble {
  max-width: 75%;
  background: #f1f5f9;
  padding: 12px 14px;
  border-radius: 14px;
  border: 1px solid #e2e8f0;
  box-shadow: 0 6px 14px rgba(15, 23, 42, 0.08);
}

.chat-item.user .chat-bubble {
  background: #ecfeff;
  border-color: #99f6e4;
}

.chat-text {
  white-space: pre-wrap;
  font-size: 14px;
}

.chat-input {
  display: flex;
  gap: 12px;
  margin-top: 12px;
}

@media (max-width: 1100px) {
  .detect-grid {
    grid-template-columns: 1fr;
  }
}
</style>