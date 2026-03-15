<template>
  <div class="permissions-page">
    <section class="panel">
      <div class="permissions-header">
        <div>
          <h2 style="margin: 0;">权限管理</h2>
          <div class="subtle">查看系统权限与接口配置</div>
        </div>
        <div class="header-actions">
          <el-input v-model="keyword" placeholder="搜索权限名称/编码/路径" clearable class="search-input" />
          <el-select v-model="typeFilter" placeholder="类型" clearable class="type-select">
            <el-option label="模块" value="MODULE" />
            <el-option label="动作" value="ACTION" />
          </el-select>
          <el-button type="primary" @click="openCreate">新增权限</el-button>
          <el-button plain :loading="loading" @click="fetchPermissions">刷新</el-button>
        </div>
      </div>
    </section>

    <section class="panel" v-loading="loading">
      <div class="kpi-grid">
        <div class="kpi-card">
          <div class="subtle">权限总数</div>
          <div class="kpi-value">{{ formatNumber(permissions.length) }}</div>
        </div>
        <div class="kpi-card">
          <div class="subtle">模块权限</div>
          <div class="kpi-value">{{ formatNumber(moduleCount) }}</div>
        </div>
        <div class="kpi-card">
          <div class="subtle">动作权限</div>
          <div class="kpi-value">{{ formatNumber(actionCount) }}</div>
        </div>
      </div>
    </section>

    <section class="panel" v-loading="loading">
      <el-table :data="filteredPermissions" style="width: 100%;">
        <el-table-column label="名称" prop="name" width="180" />
        <el-table-column label="编码" prop="code" width="200" />
        <el-table-column label="类型" width="100">
          <template #default="{ row }">
            <el-tag :type="row.type === 'MODULE' ? 'success' : 'info'">
              {{ row.type === 'MODULE' ? '模块' : '动作' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="路径" prop="path" width="200">
          <template #default="{ row }">
            {{ row.path || '--' }}
          </template>
        </el-table-column>
        <el-table-column label="方法" prop="method" width="120">
          <template #default="{ row }">
            {{ row.method || '--' }}
          </template>
        </el-table-column>
        <el-table-column label="描述">
          <template #default="{ row }">
            {{ row.description || '--' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <el-button size="small" type="danger" plain @click="onDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div v-if="!filteredPermissions.length" class="subtle empty-state">暂无权限数据</div>
    </section>

    <el-dialog v-model="dialogVisible" title="新增权限" width="560px">
      <el-form label-width="90px">
        <el-form-item label="编码">
          <el-input v-model="form.code" placeholder="例如: records" />
        </el-form-item>
        <el-form-item label="名称">
          <el-input v-model="form.name" placeholder="例如: 检测记录" />
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="form.type" placeholder="选择类型">
            <el-option label="模块" value="MODULE" />
            <el-option label="动作" value="ACTION" />
          </el-select>
        </el-form-item>
        <el-form-item label="路径">
          <el-input v-model="form.path" placeholder="可选" />
        </el-form-item>
        <el-form-item label="方法">
          <el-input v-model="form.method" placeholder="可选" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" placeholder="可选" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sortOrder" :min="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="savePermission">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { api } from '../api'
import type { Permission } from '../api/types'

const permissions = ref<Permission[]>([])
const loading = ref(false)
const saving = ref(false)
const keyword = ref('')
const typeFilter = ref<'MODULE' | 'ACTION' | ''>('')
const dialogVisible = ref(false)
const form = reactive({
  code: '',
  name: '',
  type: 'MODULE' as 'MODULE' | 'ACTION',
  path: '',
  method: '',
  description: '',
  sortOrder: 0
})

const moduleCount = computed(() => permissions.value.filter((item) => item.type === 'MODULE').length)
const actionCount = computed(() => permissions.value.filter((item) => item.type === 'ACTION').length)

const filteredPermissions = computed(() => {
  const key = keyword.value.trim().toLowerCase()
  return permissions.value.filter((item) => {
    const hitType = !typeFilter.value || item.type === typeFilter.value
    if (!key) return hitType
    const haystack = `${item.name} ${item.code} ${item.path || ''} ${item.method || ''}`.toLowerCase()
    return hitType && haystack.includes(key)
  })
})

const fetchPermissions = async () => {
  loading.value = true
  try {
    const res = await api.listPermissions()
    if (res.success) {
      permissions.value = res.data
      return
    }
    ElMessage.error(res.message || '加载权限失败')
  } catch (err: any) {
    ElMessage.error(err?.message || '加载权限失败')
  } finally {
    loading.value = false
  }
}

const openCreate = () => {
  form.code = ''
  form.name = ''
  form.type = 'MODULE'
  form.path = ''
  form.method = ''
  form.description = ''
  form.sortOrder = 0
  dialogVisible.value = true
}

const savePermission = async () => {
  if (!form.code.trim() || !form.name.trim()) {
    ElMessage.warning('请填写编码和名称')
    return
  }
  saving.value = true
  try {
    const res = await api.createPermission({
      code: form.code.trim(),
      name: form.name.trim(),
      type: form.type,
      path: form.path || undefined,
      method: form.method || undefined,
      description: form.description || undefined,
      sortOrder: form.sortOrder || 0
    })
    if (!res.success) {
      ElMessage.error(res.message || '创建失败')
      return
    }
    dialogVisible.value = false
    await fetchPermissions()
  } catch (err: any) {
    ElMessage.error(err?.message || '创建失败')
  } finally {
    saving.value = false
  }
}

const onDelete = async (permission: Permission) => {
  try {
    await ElMessageBox.confirm(`确认删除权限 ${permission.name} ?`, '提示', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消'
    })
  } catch {
    return
  }
  const res = await api.deletePermission(permission.id)
  if (res.success) {
    ElMessage.success('删除成功')
    await fetchPermissions()
    return
  }
  ElMessage.error(res.message || '删除失败')
}

const formatNumber = (value?: number) => {
  if (value === null || value === undefined) return '--'
  return Number(value).toLocaleString()
}

onMounted(fetchPermissions)
</script>

<style scoped>
.permissions-page {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.permissions-header {
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
  width: 240px;
}

.type-select {
  width: 140px;
}

.kpi-value {
  margin-top: 8px;
  font-size: 22px;
  font-weight: 700;
}

.empty-state {
  margin-top: 12px;
}
</style>
