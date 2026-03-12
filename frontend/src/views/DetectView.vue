<template>
  <div class="detect-grid">
    <section class="panel">
      <h2 style="margin-top: 0;">图像检测</h2>
      <el-form label-width="120px">
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
        <el-button type="primary" :loading="loadingDetect" @click="onDetect">开始检测</el-button>
      </el-form>

      <div class="result-block">
        <h3 style="margin-top: 0;">检测结果</h3>
        <div v-if="result">
          <p><strong>描述：</strong>{{ result.data.description }}</p>
          <p><strong>是否异常：</strong>{{ result.data.has_anomaly ? '是' : '否' }}</p>
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
          <h2 style="margin: 0;">检测对话</h2>
          <div class="subtle">基于当前图像的多轮问答</div>
        </div>
        <el-button size="small" plain @click="clearChat">清空对话</el-button>
      </div>

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
import { nextTick, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { api } from '../api'
import type { InferenceDetectResponse } from '../api/types'

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

const imageInput = ref<HTMLInputElement | null>(null)
const normalInput = ref<HTMLInputElement | null>(null)

const chatInput = ref('')
const chatBody = ref<HTMLElement | null>(null)
const messages = ref<{ role: 'user' | 'assistant'; content: string; time: string }[]>([
  { role: 'assistant', content: '请先上传图片并执行检测，我会基于图像回答问题。', time: new Date().toLocaleTimeString() }
])

const triggerImage = () => imageInput.value?.click()
const triggerNormal = () => normalInput.value?.click()

const onImageChange = (e: Event) => {
  const file = (e.target as HTMLInputElement).files?.[0] || null
  imageFile.value = file
  preview.value = file ? URL.createObjectURL(file) : null
}

const onNormalChange = (e: Event) => {
  const file = (e.target as HTMLInputElement).files?.[0] || null
  normalFile.value = file
  normalPreview.value = file ? URL.createObjectURL(file) : null
}

const callDetect = async (customPrompt?: string) => {
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
    messages.value.push({
      role: 'assistant',
      content: data.data.description,
      time: new Date().toLocaleTimeString()
    })
    await scrollChat()
  } catch (err: any) {
    ElMessage.error(err?.message || '检测失败')
  } finally {
    loadingDetect.value = false
  }
}

const onAsk = async () => {
  if (!chatInput.value.trim()) return
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
    messages.value.push({
      role: 'assistant',
      content: data.data.description,
      time: new Date().toLocaleTimeString()
    })
  } catch (err: any) {
    ElMessage.error(err?.message || '对话失败')
  } finally {
    loadingChat.value = false
    await scrollChat()
  }
}

const clearChat = () => {
  messages.value = [
    { role: 'assistant', content: '请先上传图片并执行检测，我会基于图像回答问题。', time: new Date().toLocaleTimeString() }
  ]
}

const scrollChat = async () => {
  await nextTick()
  if (chatBody.value) {
    chatBody.value.scrollTop = chatBody.value.scrollHeight
  }
}
</script>

<style scoped>
.detect-grid {
  display: grid;
  grid-template-columns: minmax(360px, 1fr) minmax(360px, 1fr);
  gap: 20px;
}

.hidden-input {
  display: none;
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
}

.upload-preview img {
  max-width: 100%;
  max-height: 220px;
  border-radius: 10px;
}

.upload-placeholder {
  color: #64748b;
}

.result-block {
  margin-top: 20px;
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

.chat-body {
  flex: 1;
  overflow-y: auto;
  padding: 12px;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  background: #ffffff;
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