<template>
  <div class="audit-page">
    <section class="panel">
      <div class="audit-header">
        <div>
          <h2 style="margin: 0;">审计日志</h2>
        </div>
        <div class="header-actions">
          <el-input v-model="keyword" placeholder="搜索描述关键词" clearable class="search-input" />
          <el-select v-model="reviewStatus" placeholder="复审状态" clearable>
            <el-option label="未审核" :value="0" />
            <el-option label="已审核" :value="1" />
          </el-select>
          <el-select v-model="reviewResult" placeholder="复审结果" clearable>
            <el-option label="通过" :value="1" />
            <el-option label="不通过" :value="0" />
          </el-select>
          <el-date-picker
            v-model="dateRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
          />
          <el-button type="primary" :loading="loading" @click="fetchRecords">查询</el-button>
          <el-button @click="resetFilters">重置</el-button>
        </div>
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
        <el-table-column label="热力图" width="120">
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
        <el-table-column label="异常" width="100">
          <template #default="{ row }">
            <el-tag :type="row.hasAnomaly ? 'danger' : 'success'">
              {{ row.hasAnomaly ? '异常' : '正常' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="复审状态" width="120">
          <template #default="{ row }">
            <el-tag v-if="row.reviewStatus === 1" type="info">已审核</el-tag>
            <el-tag v-else type="warning">未审核</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="复审结果" width="120">
          <template #default="{ row }">
            <el-tag v-if="row.reviewStatus === 1" :type="row.reviewResult === 1 ? 'success' : 'danger'">
              {{ row.reviewResult === 1 ? '通过' : '不通过' }}
            </el-tag>
            <span v-else class="subtle">--</span>
          </template>
        </el-table-column>
        <el-table-column label="复审备注">
          <template #default="{ row }">
            {{ row.reviewNote || '--' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button size="small" :disabled="row.reviewStatus === 1" @click="openReview(row, 1)">通过</el-button>
            <el-button size="small" type="danger" plain :disabled="row.reviewStatus === 1" @click="openReview(row, 0)">
              不通过
            </el-button>
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

    <el-dialog v-model="dialogVisible" :title="reviewResult === 1 ? '复审通过' : '复审不通过'" width="520px">
      <el-form label-width="90px">
        <el-form-item label="复审备注">
          <el-input
            v-model="reviewNote"
            type="textarea"
            :rows="3"
            placeholder="填写复审原因或备注"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitReview">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { api } from '../api'
import type { InspectionRecord } from '../api/types'
import { useAuthStore } from '../stores/auth'

const auth = useAuthStore()
const loading = ref(false)
const saving = ref(false)
const records = ref<InspectionRecord[]>([])
const keyword = ref('')
const reviewStatus = ref<number | null>(null)
const reviewResult = ref<number | null>(null)
const dateRange = ref<[Date, Date] | null>(null)
const page = ref(1)
const size = ref(10)
const total = ref(0)

const dialogVisible = ref(false)
const reviewTargetId = ref<number | null>(null)
const reviewNote = ref('')

const formatDateTime = (value: Date) => {
  const pad = (n: number) => String(n).padStart(2, '0')
  return `${value.getFullYear()}-${pad(value.getMonth() + 1)}-${pad(value.getDate())} ${pad(
    value.getHours()
  )}:${pad(value.getMinutes())}:${pad(value.getSeconds())}`
}

const fetchRecords = async () => {
  loading.value = true
  try {
    const params = {
      reviewStatus: reviewStatus.value === null ? undefined : reviewStatus.value,
      reviewResult: reviewResult.value === null ? undefined : reviewResult.value,
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
    ElMessage.error(res.message || '加载记录失败')
  } catch (err: any) {
    ElMessage.error(err?.message || '加载记录失败')
  } finally {
    loading.value = false
  }
}

const resetFilters = () => {
  keyword.value = ''
  reviewStatus.value = null
  reviewResult.value = null
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

const openReview = (record: InspectionRecord, result: number) => {
  reviewTargetId.value = record.id
  reviewResult.value = result
  reviewNote.value = ''
  dialogVisible.value = true
}

const submitReview = async () => {
  if (!reviewTargetId.value || reviewResult.value === null) return
  saving.value = true
  try {
    const res = await api.recordReview(reviewTargetId.value, {
      reviewResult: reviewResult.value,
      reviewNote: reviewNote.value || undefined,
      reviewerId: auth.user?.id
    })
    if (!res.success) {
      ElMessage.error(res.message || '复审提交失败')
      return
    }
    dialogVisible.value = false
    await fetchRecords()
  } catch (err: any) {
    ElMessage.error(err?.message || '复审提交失败')
  } finally {
    saving.value = false
  }
}

onMounted(fetchRecords)
</script>

<style scoped>
.audit-page {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.audit-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.search-input {
  width: 220px;
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
