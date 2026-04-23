<template>
  <div class="page">
    <h3>登录</h3>
    <el-form :model="form" @submit.prevent="onSubmit">
      <el-form-item label="用户名"><el-input v-model="form.username" /></el-form-item>
      <el-form-item label="密码"><el-input v-model="form.password" type="password" show-password /></el-form-item>
      <el-button type="primary" :loading="ld" native-type="submit">登录</el-button>
      <el-button @click="$router.push('/auth/register')">去注册</el-button>
    </el-form>
  </div>
</template>
<script setup>
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { login } from '@/api/ums'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
const form = reactive({ username: '', password: '' })
const ld = ref(false)
const u = useUserStore()
const route = useRoute()
const router = useRouter()
async function onSubmit() {
  ld.value = true
  try {
    const r = await login(form)
    u.setTokens(r.data)
    ElMessage.success('登录成功')
    router.push(route.query.redirect || '/home')
  } finally {
    ld.value = false
  }
}
</script>
