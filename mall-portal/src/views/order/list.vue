<template>
  <div class="page ignore-vw order-list-page">
    <section class="section-shell">
      <div class="section-head">
        <div>
          <h1 class="section-title">订单中心</h1>
          <div class="section-copy">展示当前用户在数码销售平台中的订单列表与状态筛选。</div>
        </div>
      </div>
      <el-select v-model="st" placeholder="选择订单状态" clearable @change="load" class="status-select">
        <el-option :value="0" label="待支付" />
        <el-option :value="1" label="待发货" />
        <el-option :value="2" label="待收货" />
        <el-option :value="3" label="已完成" />
      </el-select>
    </section>

    <el-skeleton v-if="ld" :rows="4" />

    <section v-else class="list-grid">
      <article v-for="o in rows" :key="o.id" class="section-shell order-card" @click="$router.push('/order/detail/' + o.id)">
        <div class="top">
          <strong>{{ o.orderNo }}</strong>
          <span class="status">{{ statusText(o.status) }}</span>
        </div>
        <div class="mid">订单金额 ￥{{ formatMoney(o.payAmount) }}</div>
        <div class="bottom">
          <span>创建时间 {{ o.createTime || '--' }}</span>
          <span class="link">查看订单详情</span>
        </div>
      </article>
    </section>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { myOrders } from '@/api/oms'

const st = ref(null)
const rows = ref([])
const ld = ref(true)

onMounted(load)

async function load() {
  ld.value = true
  const params = { p: 1, s: 20 }
  if (st.value !== null && st.value !== undefined && st.value !== '') {
    params.status = st.value
  }
  const result = await myOrders(params)
  rows.value = result.data?.records || []
  ld.value = false
}

function formatMoney(value) {
  const n = Number(value)
  return Number.isNaN(n) ? String(value ?? '--') : n.toFixed(2)
}

function statusText(value) {
  const map = {
    0: '待支付',
    1: '待发货',
    2: '待收货',
    3: '已完成',
    4: '已关闭',
  }
  return map[value] || ('状态 ' + value)
}
</script>

<style scoped lang="scss">
.ignore-vw.order-list-page {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.ignore-vw .status-select {
  width: 240px;
}

.ignore-vw .list-grid {
  display: grid;
  gap: 14px;
}

.ignore-vw .order-card {
  cursor: pointer;
}

.ignore-vw .top,
.ignore-vw .bottom {
  display: flex;
  justify-content: space-between;
  gap: 12px;
}

.ignore-vw .top strong {
  color: $brand-ink;
  font-size: 18px;
}

.ignore-vw .status {
  color: $brand-primary;
  font-size: 13px;
  font-weight: 700;
}

.ignore-vw .mid {
  margin-top: 14px;
  color: #ff5c43;
  font-size: 28px;
  font-weight: 800;
}

.ignore-vw .bottom {
  margin-top: 14px;
  color: $text-soft;
  font-size: 13px;
}

.ignore-vw .link {
  color: $brand-primary;
  font-weight: 700;
}

@media (max-width: 760px) {
  .ignore-vw .status-select {
    width: 100%;
  }

  .ignore-vw .top,
  .ignore-vw .bottom {
    flex-direction: column;
  }
}
</style>
