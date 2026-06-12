<template>
  <div class="page ignore-vw order-confirm-page">
    <section class="section-shell">
      <div class="section-head">
        <div>
          <h1 class="section-title">确认订单</h1>
          <div class="section-copy">完成地址确认、优惠试算与订单金额核算，构成完整的前台下单链路。</div>
        </div>
      </div>
      <div class="step-line">
        <span class="active">1 选择地址</span>
        <span class="active">2 订单试算</span>
        <span>3 提交支付</span>
      </div>
    </section>

    <el-skeleton v-if="ld" :rows="5" />

    <template v-else>
      <section class="section-shell">
        <div class="section-head">
          <div>
            <h2 class="section-title">收货地址</h2>
            <div class="section-copy">选择现有地址，或进入地址管理页新增地址。</div>
          </div>
        </div>
        <el-radio-group v-model="addressId" @change="doPreview" class="address-grid">
          <el-radio v-for="a in addrs" :key="a.id" :value="a.id" class="addr-option">
            <strong>{{ a.name }} {{ a.phone }}</strong>
            <span>{{ a.province }}{{ a.city }}{{ a.district }}{{ a.detail }}</span>
          </el-radio>
        </el-radio-group>
        <el-button v-if="!addrs.length" type="primary" text @click="$router.push('/member/address')">前往新增地址</el-button>
      </section>

      <section class="section-shell">
        <div class="section-head">
          <div>
            <h2 class="section-title">优惠试算</h2>
            <div class="section-copy">输入优惠记录 ID 可进行订单优惠金额演示。</div>
          </div>
        </div>
        <el-input v-model="couponHid" placeholder="不使用优惠可留空" @blur="doPreview" />
      </section>

      <section class="section-shell" v-if="pv">
        <div class="section-head">
          <div>
            <h2 class="section-title">订单清单</h2>
            <div class="section-copy">当前待提交订单的设备明细与金额汇总。</div>
          </div>
        </div>
        <div v-for="(line, i) in pv.lines" :key="i" class="line">
          <span>{{ line.spuName }} x{{ line.quantity }}</span>
          <strong>￥{{ formatMoney(line.lineTotal) }}</strong>
        </div>
        <div class="summary-box">
          <div><span>商品小计</span><strong>￥{{ formatMoney(pv.subtotal) }}</strong></div>
          <div><span>优惠金额</span><strong>￥{{ formatMoney(pv.discount) }}</strong></div>
          <div><span>运费</span><strong>￥{{ formatMoney(pv.freight) }}</strong></div>
          <div class="total"><span>应付金额</span><strong>￥{{ formatMoney(pv.payAmount) }}</strong></div>
        </div>
        <el-button type="primary" :loading="submitting" :disabled="!addressId" @click="submit">提交订单并前往支付</el-button>
      </section>
    </template>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { createOrder, previewOrder } from '@/api/oms'
import { listAddress } from '@/api/ums'

const addrs = ref([])
const addressId = ref()
const couponHid = ref('')
const pv = ref(null)
const ld = ref(true)
const submitting = ref(false)
const router = useRouter()

onMounted(async () => {
  const result = await listAddress()
  addrs.value = result.data || []
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
    const couponHistoryId = couponHid.value ? Number(couponHid.value) : null
    const result = await previewOrder({ addressId: addressId.value, couponHistoryId: couponHistoryId || undefined })
    pv.value = result.data
  } catch {
    pv.value = null
  }
}

async function submit() {
  submitting.value = true
  try {
    const couponHistoryId = couponHid.value ? Number(couponHid.value) : null
    const result = await createOrder({ addressId: addressId.value, couponHistoryId: couponHistoryId || undefined })
    ElMessage.success('订单已创建，正在进入支付页')
    router.push('/order/pay/' + result.data.id)
  } finally {
    submitting.value = false
  }
}

function formatMoney(value) {
  const n = Number(value)
  return Number.isNaN(n) ? String(value ?? '--') : n.toFixed(2)
}
</script>

<style scoped lang="scss">
.ignore-vw.order-confirm-page {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.ignore-vw .step-line {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.ignore-vw .step-line span {
  padding: 8px 12px;
  border-radius: 999px;
  background: $brand-surface-muted;
  color: $text-sub;
  font-size: 13px;
  font-weight: 700;
}

.ignore-vw .step-line .active {
  background: rgba(31, 107, 255, 0.09);
  color: $brand-primary;
}

.ignore-vw .address-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
}

.ignore-vw .addr-option {
  margin: 0;
  padding: 16px;
  border-radius: 18px;
  border: 1px solid $brand-border;
  background: linear-gradient(180deg, #ffffff, #f8fbff);
}

.ignore-vw .addr-option strong,
.ignore-vw .addr-option span {
  display: block;
}

.ignore-vw .addr-option strong {
  color: $brand-ink;
}

.ignore-vw .addr-option span {
  margin-top: 8px;
  color: $text-sub;
  line-height: 1.7;
}

.ignore-vw .line {
  display: flex;
  justify-content: space-between;
  gap: 10px;
  padding: 14px 0;
  border-bottom: 1px solid $brand-border;
  color: $text-sub;
}

.ignore-vw .line strong {
  color: $brand-ink;
}

.ignore-vw .summary-box {
  margin: 18px 0;
  padding: 18px;
  border-radius: 18px;
  background: linear-gradient(180deg, #f8fbff, #eef4fa);
}

.ignore-vw .summary-box div {
  display: flex;
  justify-content: space-between;
  gap: 10px;
  padding: 8px 0;
  color: $text-sub;
}

.ignore-vw .summary-box strong {
  color: $brand-ink;
}

.ignore-vw .summary-box .total strong {
  color: #ff5c43;
  font-size: 24px;
}

@media (max-width: 760px) {
  .ignore-vw .address-grid {
    grid-template-columns: 1fr;
  }
}
</style>
