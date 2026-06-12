<template>
  <div class="page auth-page ignore-vw">
    <section class="auth-card">
      <div class="auth-head">
        <p class="auth-kicker">Create Account</p>
        <h1>注册账号</h1>
        <p class="auth-copy">使用用户名、密码和图形验证码快速创建账号，注册成功后将自动进入系统。</p>
      </div>

      <el-form ref="formRef" :model="form" :rules="rules" class="auth-form" label-position="top" @submit.prevent="onSubmit">
        <el-form-item label="用户名" prop="username">
          <el-input v-model.trim="form.username" placeholder="4-20 位字母、数字或下划线" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" show-password placeholder="至少 6 位密码" />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="form.confirmPassword" type="password" show-password placeholder="请再次输入密码" />
        </el-form-item>
        <el-form-item label="图形验证码" prop="code">
          <div class="captcha-row">
            <el-input v-model.trim="form.code" maxlength="4" placeholder="请输入验证码" />
            <button class="captcha-image" type="button" @click="getCap">
              <img v-if="captchaUrl" :src="captchaUrl" alt="captcha" />
              <span v-else>加载中</span>
            </button>
            <el-button text @click="getCap">换一张</el-button>
          </div>
        </el-form-item>
        <div class="auth-actions">
          <el-button type="primary" :loading="ld" native-type="submit">注册并登录</el-button>
          <el-button @click="goLogin">返回登录</el-button>
        </div>
      </el-form>
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getCaptcha, register } from '@/api/ums'
import { useUserStore } from '@/stores/user'

const USERNAME_RE = /^[A-Za-z0-9_]{4,20}$/

const formRef = ref()
const form = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  code: '',
  captchaKey: '',
})
const captcha = ref('')
const ld = ref(false)
const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const captchaUrl = computed(() => {
  if (!captcha.value) {
    return ''
  }
  return captcha.value.startsWith('data:') ? captcha.value : `data:image/png;base64,${captcha.value}`
})

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    {
      validator: (_, value, callback) => {
        if (!USERNAME_RE.test(value || '')) {
          callback(new Error('用户名需为 4-20 位字母、数字或下划线'))
          return
        }
        callback()
      },
      trigger: 'blur',
    },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于 6 位', trigger: 'blur' },
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    {
      validator: (_, value, callback) => {
        if (value !== form.password) {
          callback(new Error('两次输入的密码不一致'))
          return
        }
        callback()
      },
      trigger: 'blur',
    },
  ],
  code: [{ required: true, message: '请输入图形验证码', trigger: 'blur' }],
}

onMounted(getCap)

async function getCap() {
  const t = await getCaptcha()
  form.captchaKey = t.data.captchaKey
  captcha.value = t.data.captchaImage
}

function goLogin() {
  const redirect = route.query.redirect
  router.push(redirect ? `/auth/login?redirect=${encodeURIComponent(redirect)}` : '/auth/login')
}

async function onSubmit() {
  const ok = await formRef.value?.validate().catch(() => false)
  if (!ok) {
    return
  }

  ld.value = true
  try {
    const payload = {
      username: form.username,
      password: form.password,
      code: form.code,
      captchaKey: form.captchaKey,
    }
    const r = await register(payload)
    userStore.setTokens(r.data)
    ElMessage.success('注册并登录成功')
    router.push(route.query.redirect || '/home')
  } catch (error) {
    form.code = ''
    await getCap()
    throw error
  } finally {
    ld.value = false
  }
}
</script>

<style scoped lang="scss">
.ignore-vw.auth-page {
  display: grid;
  place-items: start center;
  padding-top: 18px;
}

.ignore-vw .auth-card {
  width: min(100%, 560px);
  padding: 20px 22px;
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.94);
  border: 1px solid rgba(207, 220, 237, 0.9);
  box-shadow: 0 24px 48px rgba(13, 26, 44, 0.1);
}

.ignore-vw .auth-head h1 {
  margin: 6px 0 0;
  font-size: 26px;
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
  margin: 8px 0 0;
  color: $text-sub;
  font-size: 13px;
  line-height: 1.6;
}

.ignore-vw .auth-form {
  margin-top: 16px;
}

.ignore-vw .auth-form :deep(.el-form-item) {
  margin-bottom: 14px;
}

.ignore-vw .auth-form :deep(.el-form-item__label) {
  padding-bottom: 4px;
  line-height: 1.2;
}

.ignore-vw .auth-form :deep(.el-input__wrapper) {
  min-height: 40px;
}

.ignore-vw .captcha-row {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 120px auto;
  gap: 12px;
  align-items: center;
}

.ignore-vw .captcha-image {
  height: 40px;
  padding: 0;
  border: 1px solid rgba(207, 220, 237, 0.9);
  border-radius: 12px;
  overflow: hidden;
  background: #f7fbff;
  cursor: pointer;
}

.ignore-vw .captcha-image img,
.ignore-vw .captcha-image span {
  display: block;
  width: 100%;
  height: 100%;
}

.ignore-vw .captcha-image img {
  object-fit: cover;
}

.ignore-vw .captcha-image span {
  line-height: 40px;
  color: $text-sub;
  font-size: 12px;
}

.ignore-vw .auth-actions {
  display: flex;
  gap: 12px;
  margin-top: 4px;
}

.ignore-vw .auth-actions :deep(.el-button) {
  min-width: 112px;
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

  .ignore-vw .captcha-row {
    grid-template-columns: 1fr;
  }

  .ignore-vw .captcha-image {
    width: 100%;
  }

  .ignore-vw .auth-actions {
    flex-direction: column;
  }
}
</style>
