<template>
  <div class="page">
    <h3>个人中心</h3>
    <p v-if="me">欢迎，{{ me.username }}</p>
    <el-button @click="$router.push('/member/info')">资料</el-button>
    <el-button @click="$router.push('/member/address')">地址</el-button>
    <el-button @click="$router.push('/member/coupon')">领券</el-button>
    <el-button @click="$router.push('/order/list')">我的订单</el-button>
    <el-button type="danger" @click="logout">退出</el-button>
  </div>
</template>
<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getMe } from '@/api/ums'
import { useUserStore } from '@/stores/user'
const me = ref(null)
const u = useUserStore()
const router = useRouter()
onMounted(async () => {
  const t = await getMe()
  me.value = t.data
})
function logout() {
  u.clear()
  router.push('/auth/login')
}
</script>
