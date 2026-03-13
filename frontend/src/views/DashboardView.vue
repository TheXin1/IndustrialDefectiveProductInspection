<template>
  <div class="panel" style="margin-bottom: 20px;" v-loading="loading">
    <h2 style="margin-top: 0;">质量监控总览</h2>
    <div class="kpi-grid">
      <div class="kpi-card">
        <div class="subtle">今日检测量</div>
        <h3>{{ todayCountText }}</h3>
      </div>
      <div class="kpi-card">
        <div class="subtle">缺陷检出率</div>
        <h3>{{ defectRateText }}</h3>
      </div>
      <div class="kpi-card">
        <div class="subtle">实时报警</div>
        <h3>{{ alertCountText }}</h3>
      </div>
      <div class="kpi-card">
        <div class="subtle">模型版本</div>
        <h3>{{ modelVersionText }}</h3>
      </div>
    </div>
  </div>
  <div class="panel" v-loading="loading">
    <h3 style="margin-top: 0;">运行状态</h3>
    <el-descriptions :column="2" border>
      <el-descriptions-item label="推理服务">{{ inferenceStatusText }}</el-descriptions-item>
      <el-descriptions-item label="边缘节点">{{ nodeStatusText }}</el-descriptions-item>
      <el-descriptions-item label="报警策略">{{ alertPolicyText }}</el-descriptions-item>
      <el-descriptions-item label="数据同步">{{ dataSyncText }}</el-descriptions-item>
    </el-descriptions>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { api } from '../api'
import type { DashboardOverview } from '../api/types'

const overview = ref<DashboardOverview | null>(null)
const loading = ref(false)

const todayCountText = computed(() => formatNumber(overview.value?.todayCount))
const defectRateText = computed(() => {
  if (!overview.value) return '--'
  const rate = Number.isFinite(overview.value.defectRate) ? overview.value.defectRate : 0
  return `${(rate * 100).toFixed(1)}%`
})
const alertCountText = computed(() => formatNumber(overview.value?.alertCount))
const modelVersionText = computed(() => overview.value?.modelVersion || '--')
const inferenceStatusText = computed(() => overview.value?.inferenceStatus || '--')
const nodeStatusText = computed(() => overview.value?.nodeStatus || '--')
const alertPolicyText = computed(() => overview.value?.alertPolicy || '--')
const dataSyncText = computed(() => overview.value?.dataSync || '--')

const formatNumber = (value?: number) => {
  if (value === null || value === undefined) return '--'
  return Number(value).toLocaleString()
}

const loadOverview = async () => {
  loading.value = true
  try {
    const res = await api.dashboardOverview()
    if (res.success) {
      overview.value = res.data
      return
    }
    ElMessage.error(res.message || '仪表盘数据加载失败')
  } catch (err: any) {
    ElMessage.error(err?.message || '仪表盘数据加载失败')
  } finally {
    loading.value = false
  }
}

onMounted(loadOverview)
</script>
