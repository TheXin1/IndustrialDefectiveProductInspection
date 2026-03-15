<template>
  <div class="users-page">
    <section class="panel">
      <div class="users-header">
        <div>
          <h2 style="margin: 0;">用户管理</h2>
          <div class="subtle">维护系统用户与角色分配</div>
        </div>
        <div class="header-actions">
          <el-input v-model="keyword" placeholder="搜索用户名/姓名/邮箱" clearable class="search-input" />
          <el-button type="primary" @click="openCreate">新增用户</el-button>
          <el-button plain :loading="loading" @click="fetchUsers">刷新</el-button>
        </div>
      </div>
    </section>

    <section class="panel" v-loading="loading">
      <el-table :data="filteredUsers" style="width: 100%;">
        <el-table-column label="用户名" prop="username" width="160" />
        <el-table-column label="姓名" prop="displayName" width="160">
          <template #default="{ row }">
            {{ row.displayName || '--' }}
          </template>
        </el-table-column>
        <el-table-column label="邮箱" prop="email">
          <template #default="{ row }">
            {{ row.email || '--' }}
          </template>
        </el-table-column>
        <el-table-column label="手机号" prop="phone" width="140">
          <template #default="{ row }">
            {{ row.phone || '--' }}
          </template>
        </el-table-column>
        <el-table-column label="角色" width="200">
          <template #default="{ row }">
            {{ roleMap[row.id] || '--' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180">
          <template #default="{ row }">
            <el-button size="small" @click="openEdit(row)">编辑</el-button>
            <el-button size="small" type="danger" plain @click="onDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </section>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="560px">
      <el-form label-width="90px">
        <el-form-item label="用户名" v-if="mode === 'create'">
          <el-input v-model="form.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="密码" v-if="mode === 'create'">
          <el-input v-model="form.password" placeholder="请输入初始密码" show-password />
        </el-form-item>
        <el-form-item label="新密码" v-if="mode === 'edit'">
          <el-input v-model="form.password" placeholder="直接修改密码" show-password />
        </el-form-item>
        <el-form-item label="姓名">
          <el-input v-model="form.displayName" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="form.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="form.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="form.roleIds" multiple placeholder="请选择角色">
            <el-option v-for="role in roles" :key="role.id" :label="role.name" :value="role.id" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="saveUser">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { api } from '../api'
import type { Role, User } from '../api/types'

const users = ref<User[]>([])
const roles = ref<Role[]>([])
const roleMap = reactive<Record<number, string>>({})
const loading = ref(false)
const saving = ref(false)
const keyword = ref('')

const dialogVisible = ref(false)
const mode = ref<'create' | 'edit'>('create')
const dialogTitle = computed(() => (mode.value === 'create' ? '新增用户' : '编辑用户'))

const form = reactive({
  id: 0,
  username: '',
  password: '',
  displayName: '',
  email: '',
  phone: '',
  roleIds: [] as number[]
})

const filteredUsers = computed(() => {
  if (!keyword.value.trim()) return users.value
  const key = keyword.value.trim().toLowerCase()
  return users.value.filter((user) => {
    return (
      (user.username || '').toLowerCase().includes(key) ||
      (user.displayName || '').toLowerCase().includes(key) ||
      (user.email || '').toLowerCase().includes(key) ||
      (user.phone || '').toLowerCase().includes(key)
    )
  })
})

const fetchUsers = async () => {
  loading.value = true
  try {
    const res = await api.listUsers()
    if (!res.success) {
      ElMessage.error(res.message || '加载用户失败')
      return
    }
    users.value = res.data
    await loadRoleMap()
  } catch (err: any) {
    ElMessage.error(err?.message || '加载用户失败')
  } finally {
    loading.value = false
  }
}

const fetchRoles = async () => {
  const res = await api.listRoles()
  if (res.success) {
    roles.value = res.data
  }
}

const loadRoleMap = async () => {
  const tasks = users.value.map(async (user) => {
    const res = await api.userRoles(user.id)
    if (res.success) {
      roleMap[user.id] = res.data.map((role) => role.name || role.code).join(' / ')
    }
  })
  await Promise.all(tasks)
}

const resetForm = () => {
  form.id = 0
  form.username = ''
  form.password = ''
  form.displayName = ''
  form.email = ''
  form.phone = ''
  form.roleIds = []
}

const openCreate = () => {
  mode.value = 'create'
  resetForm()
  dialogVisible.value = true
}

const openEdit = async (user: User) => {
  mode.value = 'edit'
  resetForm()
  form.id = user.id
  form.displayName = user.displayName || ''
  form.email = user.email || ''
  form.phone = user.phone || ''
  const roleRes = await api.userRoles(user.id)
  if (roleRes.success) {
    form.roleIds = roleRes.data.map((role) => role.id)
  }
  dialogVisible.value = true
}

const saveUser = async () => {
  if (mode.value === 'create') {
    if (!form.username.trim() || !form.password.trim()) {
      ElMessage.warning('请输入用户名和密码')
      return
    }
  }
  saving.value = true
  try {
    if (mode.value === 'create') {
      const res = await api.createUser({
        username: form.username.trim(),
        password: form.password.trim(),
        displayName: form.displayName || undefined,
        email: form.email || undefined,
        phone: form.phone || undefined,
        roleIds: form.roleIds
      })
      if (!res.success) {
        ElMessage.error(res.message || '创建失败')
        return
      }
    } else {
      const res = await api.updateUser(form.id, {
        displayName: form.displayName || undefined,
        email: form.email || undefined,
        phone: form.phone || undefined,
        password: form.password || undefined,
        roleIds: form.roleIds
      })
      if (!res.success) {
        ElMessage.error(res.message || '保存失败')
        return
      }
    }
    dialogVisible.value = false
    await fetchUsers()
  } catch (err: any) {
    ElMessage.error(err?.message || '保存失败')
  } finally {
    saving.value = false
  }
}

const onDelete = async (user: User) => {
  try {
    await ElMessageBox.confirm(`确认删除用户 ${user.username} ?`, '提示', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消'
    })
  } catch {
    return
  }
  const res = await api.deleteUser(user.id)
  if (res.success) {
    ElMessage.success('删除成功')
    await fetchUsers()
    return
  }
  ElMessage.error(res.message || '删除失败')
}

onMounted(async () => {
  await fetchRoles()
  await fetchUsers()
})
</script>

<style scoped>
.users-page {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.users-header {
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
