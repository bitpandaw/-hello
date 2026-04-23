<template>
  <div class="login">
    <el-card class="box">
      <h2>管理端登录</h2>
      <el-form :model="f" @submit.prevent="onSubmit">
        <el-form-item label="账号">
          <el-input v-model="f.username" autocomplete="username" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="f.password" type="password" show-password autocomplete="current-password" />
        </el-form-item>
        <el-button type="primary" :loading="ld" native-type="submit" style="width: 100%">登录</el-button>
      </el-form>
    </el-card>
  </div>
</template>
<script setup>
import { reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import { login } from '@/api/admin'
import { useAdminStore } from '@/stores/admin'
import { ElMessage } from 'element-plus'
const f = reactive({ username: '', password: '' })
const ld = ref(false)
const a = useAdminStore()
const route = useRoute()
async function onSubmit() {
  ld.value = true
  try {
    const r = await login(f)
    a.setTokens(r.data)
    ElMessage.success('欢迎')
    const target = route.query.redirect || '/'
    window.location.assign(target.startsWith('/') ? target : '/')
  } finally {
    ld.value = false
  }
}
</script>
<style scoped lang="scss">
.login {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #2b32b2, #1488cc);
}
.box {
  width: 400px;
  h2 {
    text-align: center;
    margin-top: 0;
  }
}
</style>
