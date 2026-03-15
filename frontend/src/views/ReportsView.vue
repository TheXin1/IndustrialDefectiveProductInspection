<template>
  <div class="reports-page">
    <section class="panel report-hero">
      <div>
        <h2 style="margin: 0;">报表统计</h2>
        <div class="subtle">多维度汇总检测数据与异常趋势</div>
      </div>
      <div class="filter-actions">
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
        />
        <el-select v-model="status" placeholder="异常状态" clearable>
          <el-option label="异常" :value="1" />
          <el-option label="正常" :value="0" />
        </el-select>
        <el-input v-model="keyword" placeholder="关键词搜索" clearable />
        <el-button type="primary" :loading="loading" @click="fetchReport">查询</el-button>
        <el-button @click="resetFilters">重置</el-button>
      </div>
    </section>

    <section class="panel" v-loading="loading">
      <div class="kpi-grid">
        <div class="kpi-card">
          <div class="subtle">检测总量</div>
          <div class="kpi-value">{{ formatNumber(totalCount) }}</div>
        </div>
        <div class="kpi-card">
          <div class="subtle">异常数量</div>
          <div class="kpi-value">{{ formatNumber(anomalyCount) }}</div>
        </div>
        <div class="kpi-card">
          <div class="subtle">异常率</div>
          <div class="kpi-value">{{ anomalyRateText }}</div>
        </div>
        <div class="kpi-card">
          <div class="subtle">最近异常</div>
          <div class="kpi-value">{{ lastAnomalyText }}</div>
        </div>
      </div>
    </section>

    <div class="grid-2">
      <section class="panel" v-loading="loading">
        <div class="panel-title">近 7 日检测趋势</div>
        <div class="trend-bars">
          <div v-for="item in trendData" :key="item.date" class="trend-item">
            <div class="trend-bar">
              <div class="trend-fill" :style="{ height: item.percent + '%' }"></div>
            </div>
            <div class="trend-label">{{ item.label }}</div>
            <div class="trend-count">{{ item.total }}</div>
          </div>
        </div>
      </section>

      <section class="panel" v-loading="loading">
        <div class="panel-title">异常占比</div>
        <div class="ratio-card">
          <div class="pie-wrap">
            <svg class="pie-chart" viewBox="0 0 200 200">
              <circle class="pie-bg" cx="100" cy="100" r="80" />
              <circle
                class="pie-anomaly"
                cx="100"
                cy="100"
                r="80"
                :style="{ strokeDasharray: pieDash }"
              />
            </svg>
            <div class="pie-center">
              <div class="pie-value">{{ anomalyRateText }}</div>
              <div class="subtle">异常占比</div>
            </div>
          </div>
          <div class="ratio-meta">
            <div>
              <div class="subtle">异常</div>
              <div class="ratio-number">{{ formatNumber(anomalyCount) }}</div>
            </div>
            <div>
              <div class="subtle">正常</div>
              <div class="ratio-number">{{ formatNumber(normalCount) }}</div>
            </div>
          </div>
        </div>
      </section>
    </div>

    <section class="panel" v-loading="loading">
      <div class="panel-title">异常记录</div>
      <el-table :data="anomalyRecords" style="width: 100%;">
        <el-table-column label="时间" prop="createdAt" width="180" />
        <el-table-column label="描述">
          <template #default="{ row }">
            <div class="record-desc">{{ row.description || '-' }}</div>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="120">
          <template #default="{ row }">
            <el-tag type="danger">异常</el-tag>
          </template>
        </el-table-column>
      </el-table>
      <div v-if="!anomalyRecords.length" class="subtle empty-state">当前筛选条件下暂无异常记录。</div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { api } from '../api'
import type { InspectionRecord } from '../api/types'
import { useAuthStore } from '../stores/auth'

type TrendItem = {
  date: string
  label: string
  total: number
  percent: number
}

const auth = useAuthStore()
const loading = ref(false)
const records = ref<InspectionRecord[]>([])
const keyword = ref('')
const status = ref<number | null>(null)
const dateRange = ref<[Date, Date] | null>(null)

const totalCount = computed(() => records.value.length)
const anomalyCount = computed(() => records.value.filter((item) => item.hasAnomaly).length)
const normalCount = computed(() => totalCount.value - anomalyCount.value)
const anomalyRatePercent = computed(() => {
  if (!totalCount.value) return 0
  return Math.round((anomalyCount.value / totalCount.value) * 100)
})
const anomalyRateText = computed(() => `${anomalyRatePercent.value}%`)
const pieDash = computed(() => {
  const circumference = 2 * Math.PI * 80
  const rate = anomalyRatePercent.value / 100
  const filled = Math.round(circumference * rate)
  return `${filled} ${Math.max(circumference - filled, 0)}`
})
const lastAnomalyText = computed(() => {
  const times = records.value
    .filter((item) => item.hasAnomaly && item.createdAt)
    .map((item) => item.createdAt as string)
  return times.length ? times.sort().slice(-1)[0] : '--'
})
const anomalyRecords = computed(() => records.value.filter((item) => item.hasAnomaly))

const trendData = computed<TrendItem[]>(() => {
  const days = 7
  const today = new Date()
  const map = new Map<string, number>()
  for (let i = days - 1; i >= 0; i -= 1) {
    const date = new Date(today)
    date.setDate(today.getDate() - i)
    const key = date.toISOString().slice(0, 10)
    map.set(key, 0)
  }
  records.value.forEach((item) => {
    if (!item.createdAt) return
    const key = item.createdAt.slice(0, 10)
    if (map.has(key)) {
      map.set(key, (map.get(key) || 0) + 1)
    }
  })
  const values = Array.from(map.entries()).map(([date, total]) => {
    const label = date.slice(5)
    return { date, label, total }
  })
  const max = Math.max(...values.map((item) => item.total), 1)
  return values.map((item) => ({
    ...item,
    percent: Math.round((item.total / max) * 100)
  }))
})

const formatDateTime = (value: Date, endOfDay = false) => {
  const pad = (n: number) => String(n).padStart(2, '0')
  const date = new Date(value)
  if (endOfDay) {
    date.setHours(23, 59, 59, 0)
  } else {
    date.setHours(0, 0, 0, 0)
  }
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(
    date.getHours()
  )}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`
}

const fetchReport = async () => {
  loading.value = true
  try {
    const params = {
      userId: auth.user?.id,
      hasAnomaly: status.value === null ? undefined : status.value,
      keyword: keyword.value.trim() || undefined,
      startAt: dateRange.value ? formatDateTime(dateRange.value[0]) : undefined,
      endAt: dateRange.value ? formatDateTime(dateRange.value[1], true) : undefined,
      page: 1,
      size: 200
    }
    const res = await api.recordsList(params)
    if (res.success) {
      records.value = res.data.records
      return
    }
    ElMessage.error(res.message || '加载报表失败')
  } catch (err: any) {
    ElMessage.error(err?.message || '加载报表失败')
  } finally {
    loading.value = false
  }
}

const resetFilters = () => {
  keyword.value = ''
  status.value = null
  dateRange.value = null
  fetchReport()
}

const formatNumber = (value?: number) => {
  if (value === null || value === undefined) return '--'
  return Number(value).toLocaleString()
}

onMounted(fetchReport)
</script>

<style scoped>
.reports-page {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.report-hero {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
}

.filter-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  align-items: center;
}

.kpi-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
}

.kpi-card {
  background: #ffffff;
  border-radius: 14px;
  border: 1px solid var(--border);
  padding: 16px;
}

.kpi-value {
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
  font-weight: 600;
  margin-bottom: 12px;
}

.trend-bars {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 10px;
  align-items: flex-end;
  min-height: 180px;
}

.trend-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
}

.trend-bar {
  width: 100%;
  height: 140px;
  border-radius: 12px;
  background: rgba(148, 163, 184, 0.2);
  display: flex;
  align-items: flex-end;
}

.trend-fill {
  width: 100%;
  border-radius: 12px;
  background: linear-gradient(180deg, #0ea5a4 0%, #14b8a6 100%);
}

.trend-label {
  font-size: 12px;
  color: var(--muted);
}

.trend-count {
  font-size: 12px;
  font-weight: 600;
}

.ratio-card {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.ratio-meta {
  display: grid;
  grid-template-columns: repeat(2, minmax(120px, 1fr));
  gap: 12px;
}

.ratio-number {
  margin-top: 6px;
  font-weight: 600;
}

.pie-wrap {
  position: relative;
  width: 200px;
  height: 200px;
  margin: 0 auto;
}

.pie-chart {
  width: 100%;
  height: 100%;
  transform: rotate(-90deg);
}

.pie-bg {
  fill: none;
  stroke: rgba(148, 163, 184, 0.2);
  stroke-width: 20;
}

.pie-anomaly {
  fill: none;
  stroke: #ef4444;
  stroke-width: 20;
  stroke-linecap: round;
  transition: stroke-dasharray 0.3s ease;
}

.pie-center {
  position: absolute;
  inset: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.pie-value {
  font-size: 22px;
  font-weight: 700;
}

.record-desc {
  line-height: 1.6;
}

.empty-state {
  margin-top: 12px;
}

@media (max-width: 960px) {
  .report-hero {
    flex-direction: column;
  }
}
</style>
