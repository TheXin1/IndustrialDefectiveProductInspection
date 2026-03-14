<template>
  <div class="realtime-page">
    <section class="panel realtime-hero">
      <div class="hero-left">
        <div class="hero-title">实时监控</div>
        <div class="hero-sub">全链路掌握推理服务、检测趋势与异常告警</div>
        <div class="hero-meta">
          <span class="badge">{{ serviceStatusText }}</span>
          <span class="subtle">上次更新：{{ lastUpdatedText }}</span>
        </div>
      </div>
      <div class="hero-right">
        <button class="ghost-btn" :disabled="loading" @click="loadRealtime">刷新</button>
      </div>
    </section>

    <section class="panel metric-panel" v-loading="loading">
      <div class="metric-grid">
        <div class="metric-card">
          <div class="metric-label">今日检测量</div>
          <div class="metric-value">{{ formatNumber(overview?.todayCount) }}</div>
          <div class="subtle">截至当前</div>
        </div>
        <div class="metric-card">
          <div class="metric-label">缺陷检出率</div>
          <div class="metric-value">{{ defectRateText }}</div>
          <div class="subtle">今日平均</div>
        </div>
        <div class="metric-card">
          <div class="metric-label">近 1 小时异常</div>
          <div class="metric-value">{{ formatNumber(overview?.alertCount) }}</div>
          <div class="subtle">实时告警量</div>
        </div>
        <div class="metric-card">
          <div class="metric-label">模型版本</div>
          <div class="metric-value">{{ overview?.modelVersion || '--' }}</div>
          <div class="subtle">当前加载</div>
        </div>
      </div>
    </section>

    <div class="grid-2">
      <section class="panel status-panel" v-loading="loading">
        <div class="panel-title">推理服务状态</div>
        <div class="status-row">
          <div>
            <div class="status-label">推理服务</div>
            <div class="status-value">{{ serviceStatusText }}</div>
          </div>
          <div>
            <div class="status-label">边缘节点</div>
            <div class="status-value">{{ overview?.nodeStatus || '--' }}</div>
          </div>
        </div>

        <div class="progress-block">
          <div class="progress-head">
            <span>模型加载进度</span>
            <span class="subtle">{{ startupMessage }}</span>
          </div>
          <el-progress
            :percentage="startupPercent"
            :status="startupStatus === 'error' ? 'exception' : 'success'"
            :stroke-width="10"
          />
        </div>

<!--        <div class="status-grid">-->
<!--          <div class="status-item">-->
<!--            <div class="status-label">报警策略</div>-->
<!--            <div class="status-value">{{ overview?.alertPolicy || '&#45;&#45;' }}</div>-->
<!--          </div>-->
<!--          <div class="status-item">-->
<!--            <div class="status-label">数据同步</div>-->
<!--            <div class="status-value">{{ overview?.dataSync || '&#45;&#45;' }}</div>-->
<!--          </div>-->
<!--        </div>-->
      </section>

      <section class="panel alert-panel" v-loading="loading">
        <div class="panel-title">实时告警</div>
        <div v-if="alertItems.length" class="alert-list">
          <div v-for="item in alertItems" :key="item.id" class="alert-item">
            <div class="alert-dot"></div>
            <div class="alert-content">
              <div class="alert-title">{{ item.title }}</div>
              <div class="subtle">{{ item.time }}</div>
            </div>
            <div class="alert-tag">{{ item.level }}</div>
          </div>
        </div>
        <div v-else class="empty-state">暂无实时告警</div>
      </section>
    </div>

    <section class="panel trend-panel" v-loading="loading">
      <div class="panel-title">检测质量趋势</div>
      <div class="trend-grid">
        <div class="trend-card">
          <div class="trend-label">缺陷检出率</div>
          <div class="trend-value">{{ defectRateText }}</div>
          <el-progress :percentage="defectRatePercent" :stroke-width="8" />
        </div>
        <div class="trend-card">
          <div class="trend-label">异常密度</div>
          <div class="trend-value">{{ anomalyDensityText }}</div>
          <el-progress :percentage="anomalyDensityPercent" :stroke-width="8" status="warning" />
        </div>
      </div>
      <div class="subtle">后续可接入实时图表（分钟级检测量与异常分布）。</div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { api } from '../api'
import type { DashboardOverview } from '../api/types'

type StartupStatus = 'not_started' | 'loading' | 'ready' | 'error'
type AlertItem = {
  id: string
  title: string
  time: string
  level: string
}

const overview = ref<DashboardOverview | null>(null)
const loading = ref(false)
const lastUpdated = ref<string>('--')
const serviceStatus = ref<string>('未知')
const startupStatus = ref<StartupStatus>('not_started')
const startupStep = ref<number>(0)
const startupTotal = ref<number>(0)
const startupMessage = ref<string>('')
const timer = ref<number | null>(null)

const alertItems = computed<AlertItem[]>(() => {
  if (!overview.value) return []
  if (overview.value.alertCount > 0) {
    return [
      {
        id: 'alert-1',
        title: `近 1 小时异常 ${overview.value.alertCount} 次`,
        time: lastUpdated.value,
        level: '高'
      }
    ]
  }
  return []
})

const serviceStatusText = computed(() => serviceStatus.value || '未知')
const defectRateText = computed(() => {
  if (!overview.value) return '--'
  const rate = Number.isFinite(overview.value.defectRate) ? overview.value.defectRate : 0
  return `${(rate * 100).toFixed(1)}%`
})
const defectRatePercent = computed(() => {
  if (!overview.value) return 0
  const rate = Number.isFinite(overview.value.defectRate) ? overview.value.defectRate : 0
  return Math.min(100, Math.max(0, Math.round(rate * 100)))
})
const anomalyDensityText = computed(() => {
  if (!overview.value) return '--'
  const count = overview.value.alertCount || 0
  return `${count} 次/小时`
})
const anomalyDensityPercent = computed(() => {
  if (!overview.value) return 0
  const count = overview.value.alertCount || 0
  return Math.min(100, Math.max(0, count * 10))
})
const startupPercent = computed(() => {
  const total = startupTotal.value || 1
  return Math.round((startupStep.value / total) * 100)
})
const lastUpdatedText = computed(() => lastUpdated.value)

const formatNumber = (value?: number) => {
  if (value === null || value === undefined) return '--'
  return Number(value).toLocaleString()
}

const loadRealtime = async () => {
  loading.value = true
  try {
    const [overviewRes, healthRes, startupRes] = await Promise.all([
      api.dashboardOverview(),
      api.inferenceHealth(),
      api.inferenceStartup()
    ])
    if (overviewRes.success) {
      overview.value = overviewRes.data
    }
    if (healthRes.success) {
      const status = healthRes.data?.status
      serviceStatus.value = status === 'ok' ? '运行中' : '异常'
    }
    if (startupRes.success) {
      const data = startupRes.data as Record<string, any>
      startupStatus.value = (data.state as StartupStatus) || 'not_started'
      startupStep.value = Number(data.step || 0)
      startupTotal.value = Number(data.total || 1)
      startupMessage.value = String(data.message || '')
    }
    lastUpdated.value = new Date().toLocaleTimeString()
  } catch (err: any) {
    ElMessage.error(err?.message || '实时数据加载失败')
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  await loadRealtime()
  timer.value = window.setInterval(loadRealtime, 15000)
})

onBeforeUnmount(() => {
  if (timer.value) {
    window.clearInterval(timer.value)
  }
})
</script>

<style scoped>
.realtime-page {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.realtime-hero {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: linear-gradient(120deg, rgba(14, 165, 164, 0.12), rgba(15, 23, 42, 0.08));
}

.hero-title {
  font-size: 22px;
  font-weight: 700;
}

.hero-sub {
  margin-top: 6px;
  color: var(--muted);
}

.hero-meta {
  margin-top: 12px;
  display: flex;
  align-items: center;
  gap: 12px;
}

.ghost-btn {
  border: 1px solid var(--border);
  border-radius: 999px;
  padding: 8px 18px;
  background: #fff;
  cursor: pointer;
}

.ghost-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.metric-panel {
  padding-bottom: 10px;
}

.metric-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 14px;
}

.metric-card {
  background: #fff;
  border: 1px solid var(--border);
  border-radius: 14px;
  padding: 16px;
}

.metric-label {
  color: var(--muted);
  font-size: 13px;
}

.metric-value {
  margin-top: 8px;
  font-size: 22px;
  font-weight: 700;
}

.grid-2 {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(320px, 1fr));
  gap: 18px;
}

.panel-title {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 14px;
}

.status-panel {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.status-row {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(160px, 1fr));
  gap: 12px;
}

.status-label {
  color: var(--muted);
  font-size: 12px;
}

.status-value {
  margin-top: 6px;
  font-weight: 600;
}

.status-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(140px, 1fr));
  gap: 12px;
}

.progress-block {
  padding: 12px;
  border-radius: 12px;
  background: rgba(15, 23, 42, 0.04);
}

.progress-head {
  display: flex;
  justify-content: space-between;
  font-size: 13px;
  margin-bottom: 10px;
}

.alert-panel {
  min-height: 260px;
}

.alert-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.alert-item {
  display: flex;
  align-items: center;
  gap: 12px;
  border: 1px solid var(--border);
  border-radius: 12px;
  padding: 12px;
  background: #fff;
}

.alert-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: #ef4444;
}

.alert-content {
  flex: 1;
}

.alert-title {
  font-weight: 600;
}

.alert-tag {
  background: rgba(239, 68, 68, 0.12);
  color: #b91c1c;
  padding: 4px 10px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 600;
}

.empty-state {
  color: var(--muted);
  padding: 12px 0;
}

.trend-panel {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.trend-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 14px;
}

.trend-card {
  border: 1px solid var(--border);
  border-radius: 12px;
  padding: 14px;
  background: #fff;
}

.trend-label {
  color: var(--muted);
  font-size: 12px;
}

.trend-value {
  margin: 8px 0 10px;
  font-weight: 700;
}
</style>
