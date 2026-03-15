<template>
  <div class="roles-page">
    <section class="panel">
      <div class="roles-header">
        <div>
          <h2 style="margin: 0;">角色管理</h2>
          <div class="subtle">维护角色与权限绑定</div>
        </div>
        <div class="header-actions">
          <el-input v-model="keyword" placeholder="搜索角色名称/编码" clearable class="search-input" />
          <el-button type="primary" @click="openCreate">新增角色</el-button>
          <el-button plain :loading="loading" @click="fetchRoles">刷新</el-button>
        </div>
      </div>
    </section>

    <section class="panel" v-loading="loading">
      <el-table :data="filteredRoles" style="width: 100%;">
        <el-table-column label="编码" prop="code" width="160" />
        <el-table-column label="名称" prop="name" width="180" />
        <el-table-column label="描述">
          <template #default="{ row }">
            {{ row.description || '--' }}
          </template>
        </el-table-column>
        <el-table-column label="内置" width="100">
          <template #default="{ row }">
            <el-tag :type="row.builtIn ? 'info' : 'success'">
              {{ row.builtIn ? '是' : '否' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button size="small" @click="openEdit(row)">编辑</el-button>
            <el-button size="small" type="danger" plain @click="onDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </section>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px">
      <el-form label-width="90px">
        <el-form-item label="角色编码" v-if="mode === 'create'">
          <el-input v-model="form.code" placeholder="请输入角色编码" />
        </el-form-item>
        <el-form-item label="角色名称">
          <el-input v-model="form.name" placeholder="请输入角色名称" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" placeholder="请输入描述" />
        </el-form-item>
        <el-form-item label="权限">
          <el-select v-model="form.permissionIds" multiple filterable placeholder="请选择权限">
            <el-option
              v-for="permission in permissions"
              :key="permission.id"
              :label="permission.name"
              :value="permission.id"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="saveRole">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { api } from '../api'
import type { Permission, Role } from '../api/types'

const roles = ref<Role[]>([])
const permissions = ref<Permission[]>([])
const loading = ref(false)
const saving = ref(false)
const keyword = ref('')

const dialogVisible = ref(false)
const mode = ref<'create' | 'edit'>('create')
const dialogTitle = computed(() => (mode.value === 'create' ? '新增角色' : '编辑角色'))

const form = reactive({
  id: 0,
  code: '',
  name: '',
  description: '',
  permissionIds: [] as number[]
})

const filteredRoles = computed(() => {
  if (!keyword.value.trim()) return roles.value
  const key = keyword.value.trim().toLowerCase()
  return roles.value.filter((role) => {
    return (
      (role.name || '').toLowerCase().includes(key) ||
      (role.code || '').toLowerCase().includes(key)
    )
  })
})

const fetchRoles = async () => {
  loading.value = true
  try {
    const res = await api.listRoles()
    if (!res.success) {
      ElMessage.error(res.message || '加载角色失败')
      return
    }
    roles.value = res.data
  } catch (err: any) {
    ElMessage.error(err?.message || '加载角色失败')
  } finally {
    loading.value = false
  }
}

const fetchPermissions = async () => {
  const res = await api.listPermissions()
  if (res.success) {
    permissions.value = res.data
  }
}

const resetForm = () => {
  form.id = 0
  form.code = ''
  form.name = ''
  form.description = ''
  form.permissionIds = []
}

const openCreate = () => {
  mode.value = 'create'
  resetForm()
  dialogVisible.value = true
}

const openEdit = async (role: Role) => {
  mode.value = 'edit'
  resetForm()
  form.id = role.id
  form.name = role.name
  form.description = role.description || ''
  const res = await api.rolePermissions(role.id)
  if (res.success) {
    form.permissionIds = res.data
  }
  dialogVisible.value = true
}

const saveRole = async () => {
  if (!form.name.trim()) {
    ElMessage.warning('请输入角色名称')
    return
  }
  if (mode.value === 'create' && !form.code.trim()) {
    ElMessage.warning('请输入角色编码')
    return
  }
  saving.value = true
  try {
    if (mode.value === 'create') {
      const res = await api.createRole({
        code: form.code.trim(),
        name: form.name.trim(),
        description: form.description || undefined,
        permissionIds: form.permissionIds
      })
      if (!res.success) {
        ElMessage.error(res.message || '创建失败')
        return
      }
    } else {
      const res = await api.updateRole(form.id, {
        name: form.name.trim(),
        description: form.description || undefined,
        permissionIds: form.permissionIds
      })
      if (!res.success) {
        ElMessage.error(res.message || '保存失败')
        return
      }
    }
    dialogVisible.value = false
    await fetchRoles()
  } catch (err: any) {
    ElMessage.error(err?.message || '保存失败')
  } finally {
    saving.value = false
  }
}

const onDelete = async (role: Role) => {
  try {
    await ElMessageBox.confirm(`确认删除角色 ${role.name} ?`, '提示', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消'
    })
  } catch {
    return
  }
  const res = await api.deleteRole(role.id)
  if (res.success) {
    ElMessage.success('删除成功')
    await fetchRoles()
    return
  }
  ElMessage.error(res.message || '删除失败')
}

onMounted(async () => {
  await fetchPermissions()
  await fetchRoles()
})
</script>

<style scoped>
.roles-page {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.roles-header {
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
</style>
