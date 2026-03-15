<template>
  <div class="settings-page">
    <section class="panel settings-hero">
      <div>
        <h2 style="margin: 0;">个人设置</h2>
        <div class="subtle">管理个人资料与基础信息</div>
      </div>
      <el-button type="primary" :loading="saving" @click="saveProfile">保存修改</el-button>
    </section>

    <div class="grid-2">
      <section class="panel profile-panel">
        <div class="profile-header">
          <div class="avatar">{{ avatarText }}</div>
          <div>
            <div class="profile-name">{{ auth.user?.displayName || auth.user?.username || '未命名' }}</div>
            <div class="subtle">{{ auth.user?.username || '--' }}</div>
          </div>
        </div>

        <el-form label-width="90px" class="profile-form">
          <el-form-item label="用户名">
            <el-input :model-value="auth.user?.username" disabled />
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
        <el-form-item label="新密码">
          <el-input v-model="form.password" placeholder="直接修改密码" show-password />
        </el-form-item>
      </el-form>
      </section>

      <section class="panel security-panel">
        <div class="panel-title">账户信息</div>
        <div class="info-card">
          <div class="info-item">
            <div class="subtle">当前角色</div>
            <div class="info-value">{{ roleText }}</div>
          </div>
          <div class="info-item">
            <div class="subtle">权限数量</div>
            <div class="info-value">{{ auth.permissions.length }}</div>
          </div>
        </div>

        <div class="tip-card">
          <div class="tip-title">提示</div>
          <div class="subtle">
            密码修改、绑定验证等安全功能可在后端开放接口后接入。
          </div>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { api } from '../api'
import { useAuthStore } from '../stores/auth'

const auth = useAuthStore()
const saving = ref(false)

const form = reactive({
  displayName: auth.user?.displayName || '',
  email: auth.user?.email || '',
  phone: auth.user?.phone || '',
  password: ''
})

const avatarText = computed(() => {
  const base = auth.user?.displayName || auth.user?.username || ''
  return base ? base.slice(0, 1).toUpperCase() : '?'
})

const roleText = computed(() => {
  if (!auth.roles.length) return '--'
  return auth.roles.map((role) => role.name || role.code).join(' / ')
})

const saveProfile = async () => {
  if (!auth.user?.id) {
    ElMessage.warning('未获取到用户信息')
    return
  }
  saving.value = true
  try {
    const payload = {
      displayName: form.displayName || undefined,
      email: form.email || undefined,
      phone: form.phone || undefined,
      password: form.password || undefined
    }
    const res = await api.updateUser(auth.user.id, payload)
    if (!res.success) {
      ElMessage.error(res.message || '保存失败')
      return
    }
    auth.user = res.data
    const storagePayload = {
      user: res.data,
      roles: auth.roles,
      permissions: auth.permissions
    }
    localStorage.setItem('industrial_auth', JSON.stringify(storagePayload))
    form.password = ''
    ElMessage.success('保存成功')
  } catch (err: any) {
    ElMessage.error(err?.message || '保存失败')
  } finally {
    saving.value = false
  }
}
</script>

<style scoped>
.settings-page {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.settings-hero {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.grid-2 {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(320px, 1fr));
  gap: 18px;
}

.profile-panel {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.profile-header {
  display: flex;
  align-items: center;
  gap: 14px;
}

.avatar {
  width: 54px;
  height: 54px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(14, 165, 164, 0.12);
  color: #0f766e;
  font-size: 22px;
  font-weight: 700;
}

.profile-name {
  font-size: 18px;
  font-weight: 700;
}

.profile-form {
  margin-top: 6px;
}

.security-panel {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.panel-title {
  font-weight: 600;
}

.info-card {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(140px, 1fr));
  gap: 12px;
}

.info-item {
  background: #fff;
  border: 1px solid var(--border);
  border-radius: 12px;
  padding: 12px;
}

.info-value {
  margin-top: 6px;
  font-weight: 600;
}

.tip-card {
  border: 1px dashed var(--border);
  border-radius: 12px;
  padding: 12px;
  background: rgba(148, 163, 184, 0.08);
}

.tip-title {
  font-weight: 600;
  margin-bottom: 6px;
}
</style>
