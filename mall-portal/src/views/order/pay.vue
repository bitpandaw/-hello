<template>
  <div class="page">
    <h3>模拟支付</h3>
    <p>订单 #{{ id }}</p>
    <el-button type="primary" :loading="ld" @click="go">支付成功</el-button>
  </div>
</template>
<script setup>
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { mockPay } from '@/api/pay'
import { ElMessage } from 'element-plus'
const route = useRoute()
const router = useRouter()
const id = ref(route.params.id)
const ld = ref(false)
async function go() {
  ld.value = true
  try {
    await mockPay(id.value)
    ElMessage.success('支付成功')
    router.push('/order/detail/' + id.value)
  } finally {
    ld.value = false
  }
}
</script>
