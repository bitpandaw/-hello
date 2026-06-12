<template>
  <div class="page ignore-vw order-detail-page">
    <el-skeleton v-if="ld" :rows="5" />
    <template v-else-if="d.order">
      <section class="section-shell">
        <div class="section-head">
          <div>
            <h1 class="section-title">订单详情</h1>
            <div class="section-copy">查看订单编号、金额、状态与设备明细，用于展示完整订单信息页。</div>
          </div>
        </div>
        <div class="info-grid">
          <div class="info-cell"><span>订单编号</span><strong>{{ d.order.orderNo }}</strong></div>
          <div class="info-cell"><span>订单状态</span><strong>{{ statusText(d.order.status) }}</strong></div>
          <div class="info-cell"><span>支付金额</span><strong>￥{{ formatMoney(d.order.payAmount) }}</strong></div>
          <div class="info-cell"><span>创建时间</span><strong>{{ d.order.createTime || '--' }}</strong></div>
        </div>
      </section>

      <section class="section-shell">
        <div class="section-head">
          <div>
            <h2 class="section-title">设备明细</h2>
            <div class="section-copy">当前订单所包含的数码设备列表。</div>
          </div>
        </div>
        <div v-for="item in d.items" :key="item.id || item.skuId" class="line">
          <div>
            <strong>{{ item.spuName }}</strong>
            <span>{{ item.specJson }}</span>
          </div>
          <div class="line-side">
            <span>x{{ item.quantity }}</span>
            <strong>￥{{ formatMoney(item.price) }}</strong>
          </div>
        </div>
      </section>
    </template>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { orderDetail } from '@/api/oms'

const route = useRoute()
const d = ref({ order: null, items: [] })
const ld = ref(true)

onMounted(async () => {
  const result = await orderDetail(route.params.id)
  d.value = result.data || d.value
  ld.value = false
})

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

function formatMoney(value) {
  const n = Number(value)
  return Number.isNaN(n) ? String(value ?? '--') : n.toFixed(2)
}
</script>

<style scoped lang="scss">
.ignore-vw.order-detail-page {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.ignore-vw .info-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
}

.ignore-vw .info-cell {
  padding: 18px;
  border-radius: 18px;
  border: 1px solid $brand-border;
  background: linear-gradient(180deg, #ffffff, #f8fbff);
}

.ignore-vw .info-cell span {
  display: block;
  color: $text-soft;
  font-size: 13px;
}

.ignore-vw .info-cell strong {
  display: block;
  margin-top: 10px;
  color: $brand-ink;
  font-size: 20px;
}

.ignore-vw .line {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  padding: 16px 0;
  border-bottom: 1px solid $brand-border;
}

.ignore-vw .line:last-child {
  border-bottom: none;
}

.ignore-vw .line strong {
  display: block;
  color: $brand-ink;
}

.ignore-vw .line span {
  display: block;
  margin-top: 8px;
  color: $text-sub;
  font-size: 13px;
}

.ignore-vw .line-side {
  text-align: right;
}

@media (max-width: 760px) {
  .ignore-vw .info-grid {
    grid-template-columns: 1fr;
  }

  .ignore-vw .line {
    flex-direction: column;
  }

  .ignore-vw .line-side {
    text-align: left;
  }
}
</style>
