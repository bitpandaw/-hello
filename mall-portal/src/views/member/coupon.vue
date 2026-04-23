<template>
  <div class="page">
    <h3>优惠券</h3>
    <el-skeleton v-if="ld" :rows="3" />
    <el-card v-else v-for="c in list" :key="c.id" class="c">
      <div>{{ c.name }} 面额 {{ c.amount }} 满 {{ c.minPoint }} 可用</div>
      <el-button size="small" @click="t(c.id)">领取</el-button>
    </el-card>
  </div>
</template>
<script setup>
import { ref, onMounted } from 'vue'
import { coupons, take } from '@/api/sms'
import { ElMessage } from 'element-plus'
const list = ref([])
const ld = ref(true)
onMounted(async () => {
  list.value = (await coupons()).data || []
  ld.value = false
})
async function t(id) {
  await take(id)
  ElMessage.success('已领取，下单时在确认页填 sms_coupon_history 中对应记录 id 试算')
}
</script>
<style scoped>
.c {
  margin-bottom: 0.16rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
