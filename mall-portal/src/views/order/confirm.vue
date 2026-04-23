<template>
  <div class="page">
    <h3>确认订单</h3>
    <el-skeleton v-if="ld" :rows="4" />
    <template v-else>
      <p>收货地址</p>
      <el-radio-group v-model="addressId" @change="doPreview" class="block">
        <el-radio v-for="a in addrs" :key="a.id" :value="a.id" style="display: block; margin: 0.12rem 0">
          {{ a.province }}{{ a.city }}{{ a.district }}{{ a.detail }} {{ a.name }} {{ a.phone }}
        </el-radio>
      </el-radio-group>
      <el-button v-if="!addrs.length" type="primary" text @click="$router.push('/member/address')">去添加地址</el-button>
      <p>领券记录 ID（可选，领券后从 DB 或管理端查 ID）</p>
      <el-input v-model="couponHid" placeholder="留空则不用券" @blur="doPreview" />
      <el-divider />
      <div v-if="pv">
        <div v-for="(l, i) in pv.lines" :key="i" class="line">
          <span>{{ l.spuName }} x{{ l.quantity }}</span>
          <span>￥{{ l.lineTotal }}</span>
        </div>
        <p>小计 ￥{{ pv.subtotal }} 优惠 ￥{{ pv.discount }} 运费 ￥{{ pv.freight }}</p>
        <h3>应付 ￥{{ pv.payAmount }}</h3>
      </div>
      <el-button type="primary" :loading="submitting" :disabled="!addressId" @click="submit">提交订单</el-button>
    </template>
  </div>
</template>
<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { listAddress } from '@/api/ums'
import { previewOrder, createOrder } from '@/api/oms'
import { ElMessage } from 'element-plus'
const addrs = ref([])
const addressId = ref()
const couponHid = ref('')
const pv = ref(null)
const ld = ref(true)
const submitting = ref(false)
const router = useRouter()
onMounted(async () => {
  const t = await listAddress()
  addrs.value = t.data || []
  if (addrs.value[0]) {
    addressId.value = addrs.value[0].id
  }
  await doPreview()
  ld.value = false
})
async function doPreview() {
  if (!addressId.value) {
    return
  }
  try {
    const ch = couponHid.value ? Number(couponHid.value) : null
    const r = await previewOrder({ addressId: addressId.value, couponHistoryId: ch || undefined })
    pv.value = r.data
  } catch {
    pv.value = null
  }
}
async function submit() {
  submitting.value = true
  try {
    const ch = couponHid.value ? Number(couponHid.value) : null
    const r = await createOrder({ addressId: addressId.value, couponHistoryId: ch || undefined })
    const o = r.data
    ElMessage.success('订单已创建')
    router.push('/order/pay/' + o.id)
  } finally {
    submitting.value = false
  }
}
</script>
<style scoped>
.line {
  display: flex;
  justify-content: space-between;
  margin: 0.12rem 0;
}
.block {
  display: block;
  width: 100%;
}
</style>
