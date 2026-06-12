<template>
  <div class="page ignore-vw">
    <section class="section-shell">
      <div class="section-head">
        <div>
          <h1 class="section-title">优惠权益</h1>
          <div class="section-copy">用于展示优惠券领取与结算流程中的权益使用能力。</div>
        </div>
      </div>
      <el-skeleton v-if="ld" :rows="3" />
      <div v-else class="coupon-grid">
        <article v-for="c in list" :key="c.id" class="coupon-card">
          <div>
            <strong>{{ c.name }}</strong>
            <p>面额 {{ c.amount }} 元，满 {{ c.minPoint }} 元可用</p>
          </div>
          <el-button size="small" @click="takeOne(c.id)">立即领取</el-button>
        </article>
      </div>
    </section>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { coupons, take } from '@/api/sms'

const list = ref([])
const ld = ref(true)

onMounted(async () => {
  list.value = (await coupons()).data || []
  ld.value = false
})

async function takeOne(id) {
  await take(id)
  ElMessage.success('优惠权益已领取，可在确认订单时使用对应记录。')
}
</script>

<style scoped lang="scss">
.ignore-vw .coupon-grid {
  display: grid;
  gap: 14px;
}

.ignore-vw .coupon-card {
  padding: 18px;
  border-radius: 18px;
  border: 1px solid $brand-border;
  background: linear-gradient(135deg, rgba(31, 107, 255, 0.06), rgba(0, 184, 217, 0.05));
  display: flex;
  justify-content: space-between;
  gap: 16px;
  align-items: center;
}

.ignore-vw .coupon-card strong {
  display: block;
  color: $brand-ink;
  font-size: 18px;
}

.ignore-vw .coupon-card p {
  margin: 8px 0 0;
  color: $text-sub;
  font-size: 14px;
}

@media (max-width: 760px) {
  .ignore-vw .coupon-card {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
