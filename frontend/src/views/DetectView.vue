<template>
  <div class="panel" style="margin-bottom: 20px;">
    <h2 style="margin-top: 0;">图像检测</h2>
    <el-form label-width="120px">
      <el-form-item label="待检图片">
        <input type="file" accept="image/*" @change="onImageChange" />
      </el-form-item>
      <el-form-item label="正常参考图">
        <input type="file" accept="image/*" @change="onNormalChange" />
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
      <el-button type="primary" :loading="loading" @click="onDetect">开始检测</el-button>
    </el-form>
  </div>

  <div class="panel">
    <h3 style="margin-top: 0;">检测结果</h3>
    <div v-if="result">
      <p><strong>描述：</strong>{{ result.data.description }}</p>
      <p><strong>是否异常：</strong>{{ result.data.has_anomaly ? '是' : '否' }}</p>
      <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(260px, 1fr)); gap: 12px;">
        <div>
          <div class="subtle">原图</div>
          <img v-if="preview" :src="preview" style="max-width: 100%; border-radius: 12px;" />
        </div>
        <div>
          <div class="subtle">定位热力图</div>
          <img v-if="heatmap" :src="heatmap" style="max-width: 100%; border-radius: 12px;" />
        </div>
      </div>
    </div>
    <div v-else class="subtle">暂无检测结果</div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { api } from '../api'
import type { InferenceDetectResponse } from '../api/types'

const imageFile = ref<File | null>(null)
const normalFile = ref<File | null>(null)
const prompt = ref('Describe this image.')
const maxTgtLen = ref(128)
const topP = ref(0.01)
const temperature = ref(1)
const loading = ref(false)
const result = ref<InferenceDetectResponse | null>(null)
const preview = ref<string | null>(null)
const heatmap = ref<string | null>(null)

const onImageChange = (e: Event) => {
  const file = (e.target as HTMLInputElement).files?.[0] || null
  imageFile.value = file
  preview.value = file ? URL.createObjectURL(file) : null
}

const onNormalChange = (e: Event) => {
  const file = (e.target as HTMLInputElement).files?.[0] || null
  normalFile.value = file
}

const onDetect = async () => {
  if (!imageFile.value) {
    ElMessage.warning('请先选择待检图片')
    return
  }
  loading.value = true
  try {
    const form = new FormData()
    form.append('image', imageFile.value)
    if (normalFile.value) form.append('normal_image', normalFile.value)
    form.append('prompt', prompt.value)
    form.append('max_tgt_len', String(maxTgtLen.value))
    form.append('top_p', String(topP.value))
    form.append('temperature', String(temperature.value))

    const res = await api.detect(form)
    if (!res.success) {
      throw new Error(res.message || 'detect_failed')
    }
    result.value = res.data
    heatmap.value = `data:image/png;base64,${res.data.data.localization_image_base64}`
  } catch (err: any) {
    ElMessage.error(err?.message || '检测失败')
  } finally {
    loading.value = false
  }
}
</script>
