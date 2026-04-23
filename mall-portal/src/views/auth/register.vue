<template>
  <div class="page">
    <h3>注册</h3>
    <p v-if="captcha" class="cap" @click="getCap"
      ><img :src="captchaUrl" alt="captcha"
    /></p>
    <el-button size="small" @click="getCap">换一张</el-button>
    <el-form :model="form" @submit.prevent="onSubmit">
      <el-form-item label="用户名"><el-input v-model="form.username" /></el-form-item>
      <el-form-item label="密码"><el-input v-model="form.password" type="password" /></el-form-item>
      <el-form-item label="验证码"><el-input v-model="form.code" /></el-form-item>
      <el-form-item label="手机(选)"><el-input v-model="form.phone" /></el-form-item>
      <el-button type="primary" :loading="ld" native-type="submit">注册</el-button>
    </el-form>
  </div>
</template>
<script setup>
import { reactive, ref, onMounted, computed } from 'vue'
import { getCaptcha, register } from '@/api/ums'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
const form = reactive({ username: '', password: '', code: '', phone: '', captchaKey: '' })
const captcha = ref('')
const captchaUrl = computed(() => {
  const s = captcha.value
  if (!s) {
    return ''
  }
  return s.startsWith('data:') ? s : 'data:image/png;base64,' + s
})
const ld = ref(false)
const router = useRouter()
onMounted(getCap)
async function getCap() {
  const t = await getCaptcha()
  form.captchaKey = t.data.captchaKey
  captcha.value = t.data.captchaImage
}
async function onSubmit() {
  ld.value = true
  try {
    await register(form)
    ElMessage.success('请登录')
    router.push('/auth/login')
  } finally {
    ld.value = false
  }
}
</script>
<style scoped>
.cap img {
  max-width: 3rem;
}
</style>
