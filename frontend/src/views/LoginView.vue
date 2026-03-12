<template>
  <div class="panel" style="max-width: 420px; margin: 80px auto;">
    <h2 style="margin-top: 0;">登录</h2>
    <el-form :model="form" @submit.prevent="onSubmit">
      <el-form-item label="用户名">
        <el-input v-model="form.username" placeholder="请输入用户名" />
      </el-form-item>
      <el-form-item label="密码">
        <el-input v-model="form.password" type="password" placeholder="请输入密码" />
      </el-form-item>
      <el-button type="primary" @click="onSubmit" :loading="loading" style="width: 100%;">
        登录
      </el-button>
    </el-form>
    <p class="subtle" style="margin-top: 12px;">提示：默认未启用权限拦截，仅基于登录返回菜单权限。</p>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '../stores/auth'

const auth = useAuthStore()
const router = useRouter()
const loading = ref(false)

const form = reactive({
  username: '',
  password: ''
})

const onSubmit = async () => {
  loading.value = true
  try {
    await auth.login(form.username, form.password)
    ElMessage.success('登录成功')
    router.push('/dashboard')
  } catch (err: any) {
    ElMessage.error(err?.message || '登录失败')
  } finally {
    loading.value = false
  }
}
</script>
