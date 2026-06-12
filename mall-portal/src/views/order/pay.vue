<template>
  <div class="page ignore-vw">
    <section class="section-shell pay-shell">
      <span class="device-chip">模拟支付流程</span>
      <h1 class="section-title">订单支付</h1>
      <div class="section-copy">用于展示毕业设计中的支付完成流程，点击后将模拟支付成功并跳转订单详情页。</div>
      <div class="pay-card">
        <span>订单编号</span>
        <strong>#{{ id }}</strong>
      </div>
      <el-button type="primary" :loading="ld" class="pay-btn" @click="go">确认支付成功</el-button>
    </section>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { mockPay } from '@/api/pay'

const route = useRoute()
const router = useRouter()
const id = ref(route.params.id)
const ld = ref(false)

async function go() {
  ld.value = true
  try {
    await mockPay(id.value)
    ElMessage.success('模拟支付成功')
    router.push('/order/detail/' + id.value)
  } finally {
    ld.value = false
  }
}
</script>

<style scoped lang="scss">
.ignore-vw .pay-shell {
  max-width: 720px;
  margin: 0 auto;
  text-align: center;
}

.ignore-vw .pay-card {
  margin: 24px auto 0;
  padding: 24px;
  border-radius: 20px;
  background: linear-gradient(180deg, #f8fbff, #eef4fa);
}

.ignore-vw .pay-card span {
  display: block;
  color: $text-soft;
  font-size: 13px;
}

.ignore-vw .pay-card strong {
  display: block;
  margin-top: 10px;
  color: $brand-ink;
  font-size: 34px;
}

.ignore-vw .pay-btn {
  margin-top: 20px;
  min-width: 220px;
  height: 48px;
  border: none;
  border-radius: 999px;
  background: linear-gradient(135deg, $brand-primary, $brand-accent);
}
</style>
