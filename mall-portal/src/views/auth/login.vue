<template>
  <div class="page auth-page ignore-vw">
    <section class="auth-card">
      <div class="auth-head">
        <p class="auth-kicker">Account Access</p>
        <h1>登录账号</h1>
        <p class="auth-copy">输入用户名和密码后即可继续浏览商品、管理购物车和查看订单。</p>
      </div>

      <el-form :model="form" class="auth-form" label-position="top" @submit.prevent="onSubmit">
        <el-form-item label="用户名">
          <el-input v-model.trim="form.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="form.password" type="password" show-password placeholder="请输入密码" />
        </el-form-item>
        <div class="auth-actions">
          <el-button type="primary" :loading="ld" native-type="submit">登录</el-button>
          <el-button @click="goRegister">去注册</el-button>
        </div>
      </el-form>
    </section>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { login } from '@/api/ums'
import { useUserStore } from '@/stores/user'

const form = reactive({ username: '', password: '' })
const ld = ref(false)
const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

function goRegister() {
  const redirect = route.query.redirect
  router.push(redirect ? `/auth/register?redirect=${encodeURIComponent(redirect)}` : '/auth/register')
}

async function onSubmit() {
  ld.value = true
  try {
    const r = await login(form)
    userStore.setTokens(r.data)
    ElMessage.success('登录成功')
    router.push(route.query.redirect || '/home')
  } finally {
    ld.value = false
  }
}
</script>

<style scoped lang="scss">
.ignore-vw.auth-page {
  display: grid;
  place-items: start center;
  padding-top: 32px;
}

.ignore-vw .auth-card {
  width: min(100%, 520px);
  padding: 28px;
  border-radius: 24px;
  background: rgba(255, 255, 255, 0.94);
  border: 1px solid rgba(207, 220, 237, 0.9);
  box-shadow: 0 24px 48px rgba(13, 26, 44, 0.1);
}

.ignore-vw .auth-head h1 {
  margin: 8px 0 0;
  font-size: 30px;
  color: $brand-ink;
}

.ignore-vw .auth-kicker {
  margin: 0;
  color: $brand-primary;
  font-size: 12px;
  font-weight: 800;
  letter-spacing: 0.12em;
  text-transform: uppercase;
}

.ignore-vw .auth-copy {
  margin: 10px 0 0;
  color: $text-sub;
  line-height: 1.7;
}

.ignore-vw .auth-form {
  margin-top: 24px;
}

.ignore-vw .auth-actions {
  display: flex;
  gap: 12px;
  margin-top: 10px;
}

.ignore-vw .auth-actions :deep(.el-button) {
  min-width: 116px;
}

@media (max-width: 640px) {
  .ignore-vw.auth-page {
    padding-top: 12px;
  }

  .ignore-vw .auth-card {
    padding: 18px;
    border-radius: 18px;
  }

  .ignore-vw .auth-head h1 {
    font-size: 24px;
  }

  .ignore-vw .auth-actions {
    flex-direction: column;
  }
}
</style>
