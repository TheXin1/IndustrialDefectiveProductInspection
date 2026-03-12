<template>
  <div class="panel" style="max-width: 480px; margin: 80px auto;">
    <h2 style="margin-top: 0;">注册</h2>
    <el-form :model="form" @submit.prevent="onSubmit">
      <el-form-item label="用户名">
        <el-input v-model="form.username" placeholder="请输入用户名" />
      </el-form-item>
      <el-form-item label="密码">
        <el-input v-model="form.password" type="password" placeholder="请输入密码" />
      </el-form-item>
      <el-form-item label="姓名">
        <el-input v-model="form.displayName" placeholder="可选" />
      </el-form-item>
      <el-form-item label="邮箱">
        <el-input v-model="form.email" placeholder="可选" />
      </el-form-item>
      <el-form-item label="电话">
        <el-input v-model="form.phone" placeholder="可选" />
      </el-form-item>
      <el-button type="primary" @click="onSubmit" :loading="loading" style="width: 100%;">
        注册
      </el-button>
    </el-form>
    <p class="subtle" style="margin-top: 12px;">
      已有账号？<el-link type="primary" @click="goLogin">去登录</el-link>
    </p>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { api } from '../api'

const router = useRouter()
const loading = ref(false)

const form = reactive({
  username: '',
  password: '',
  displayName: '',
  email: '',
  phone: ''
})

const onSubmit = async () => {
  if (!form.username || !form.password) {
    ElMessage.warning('请输入用户名和密码')
    return
  }
  loading.value = true
  try {
    const res = await api.register({
      username: form.username,
      password: form.password,
      displayName: form.displayName || undefined,
      email: form.email || undefined,
      phone: form.phone || undefined,
      status: 1,
      roleIds: [2]
    })
    if (!res.success) {
      throw new Error(res.message || 'register_failed')
    }
    ElMessage.success('注册成功，请登录')
    router.push('/login')
  } catch (err: any) {
    ElMessage.error(err?.message || '注册失败')
  } finally {
    loading.value = false
  }
}

const goLogin = () => router.push('/login')
</script>