<template>
  <div class="records-page">
    <section class="panel">
      <div class="records-header">
        <div>
          <h2 style="margin: 0;">检测记录</h2>
          <div class="subtle">追踪历史检测结果与异常分布</div>
        </div>
        <el-button type="primary" plain :loading="loading" @click="fetchRecords">刷新</el-button>
      </div>

      <div class="filter-bar">
        <el-input v-model="keyword" placeholder="搜索描述关键词" clearable class="filter-item" />
        <el-select v-model="status" placeholder="异常状态" clearable class="filter-item">
          <el-option label="异常" :value="1" />
          <el-option label="正常" :value="0" />
        </el-select>
        <el-date-picker
          v-model="dateRange"
          type="datetimerange"
          range-separator="至"
          start-placeholder="开始时间"
          end-placeholder="结束时间"
          class="filter-item"
        />
        <el-button type="primary" :loading="loading" @click="onSearch">查询</el-button>
        <el-button @click="onReset">重置</el-button>
      </div>
    </section>

    <section class="panel" v-loading="loading">
      <el-table :data="records" style="width: 100%;">
        <el-table-column label="时间" prop="createdAt" width="180" />
        <el-table-column label="描述">
          <template #default="{ row }">
            <div class="record-desc">{{ row.description || '-' }}</div>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <el-tag :type="row.hasAnomaly ? 'danger' : 'success'">
              {{ row.hasAnomaly ? '异常' : '正常' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="模型版本" prop="modelVersion" width="160">
          <template #default="{ row }">
            {{ row.modelVersion || '--' }}
          </template>
        </el-table-column>
        <el-table-column label="来源" prop="sourceType" width="120">
          <template #default="{ row }">
            {{ row.sourceType || '--' }}
          </template>
        </el-table-column>
        <el-table-column label="待检图" width="120">
          <template #default="{ row }">
            <el-image
              v-if="row.imageUrl"
              :src="row.imageUrl"
              :preview-src-list="[row.imageUrl]"
              fit="cover"
              class="thumb"
            />
            <span v-else class="subtle">暂无</span>
          </template>
        </el-table-column>
        <el-table-column label="缺陷图" width="120">
          <template #default="{ row }">
            <el-image
              v-if="row.localizationImageUrl"
              :src="row.localizationImageUrl"
              :preview-src-list="[row.localizationImageUrl]"
              fit="cover"
              class="thumb"
            />
            <span v-else class="subtle">暂无</span>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-bar">
        <el-pagination
          background
          layout="total, sizes, prev, pager, next"
          :total="total"
          :page-size="size"
          :current-page="page"
          @current-change="onPageChange"
          @size-change="onSizeChange"
        />
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { api } from '../api'
import type { InspectionRecord } from '../api/types'
import { useAuthStore } from '../stores/auth'

const auth = useAuthStore()
const records = ref<InspectionRecord[]>([])
const loading = ref(false)
const keyword = ref('')
const status = ref<number | null>(null)
const dateRange = ref<[Date, Date] | null>(null)
const page = ref(1)
const size = ref(10)
const total = ref(0)

const formatDateTime = (value: Date) => {
  const pad = (n: number) => String(n).padStart(2, '0')
  return `${value.getFullYear()}-${pad(value.getMonth() + 1)}-${pad(value.getDate())} ${pad(value.getHours())}:${pad(value.getMinutes())}:${pad(value.getSeconds())}`
}

const fetchRecords = async () => {
  loading.value = true
  try {
    const params = {
      userId: auth.user?.id,
      hasAnomaly: status.value === null ? undefined : status.value,
      keyword: keyword.value.trim() || undefined,
      startAt: dateRange.value ? formatDateTime(dateRange.value[0]) : undefined,
      endAt: dateRange.value ? formatDateTime(dateRange.value[1]) : undefined,
      page: page.value,
      size: size.value
    }
    const res = await api.recordsList(params)
    if (res.success) {
      records.value = res.data.records
      total.value = res.data.total
      return
    }
    ElMessage.error(res.message || '加载检测记录失败')
  } catch (err: any) {
    ElMessage.error(err?.message || '加载检测记录失败')
  } finally {
    loading.value = false
  }
}

const onSearch = () => {
  page.value = 1
  fetchRecords()
}

const onReset = () => {
  keyword.value = ''
  status.value = null
  dateRange.value = null
  page.value = 1
  fetchRecords()
}

const onPageChange = (next: number) => {
  page.value = next
  fetchRecords()
}

const onSizeChange = (next: number) => {
  size.value = next
  page.value = 1
  fetchRecords()
}

onMounted(fetchRecords)
</script>

<style scoped>
.records-page {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.records-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.filter-bar {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  align-items: center;
}

.filter-item {
  min-width: 200px;
}

.record-desc {
  line-height: 1.6;
}

.thumb {
  width: 64px;
  height: 64px;
  border-radius: 8px;
  border: 1px solid var(--border);
}

.pagination-bar {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
