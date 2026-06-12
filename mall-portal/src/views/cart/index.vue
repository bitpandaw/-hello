<template>
  <div class="page cart-page ignore-vw">
    <section class="section-shell intro">
      <h1 class="section-title">购物车结算</h1>
      <div class="section-copy">展示当前用户已选设备、规格信息与结算概览，形成完整的数码商城下单流程。</div>
    </section>

    <el-skeleton v-if="ld" :rows="5" />
    <el-empty v-else-if="!entries.length" description="购物车中还没有已选设备" class="section-shell empty-shell" />

    <div v-else class="cart-layout">
      <section class="section-shell cart-list">
        <div class="section-head">
          <div>
            <h2 class="section-title">设备清单</h2>
            <div class="section-copy">可勾选结算、删除商品并查看规格配置。</div>
          </div>
        </div>
        <article v-for="[k, v] in entries" :key="k" class="cart-item">
          <el-checkbox :model-value="v.selected" @change="(on) => onSel(k, on)" />
          <div class="device-thumb">{{ specText(v, k).slice(0, 2) }}</div>
          <div class="item-main">
            <strong>{{ specText(v, k) }}</strong>
            <span>单价 ￥{{ formatMoney(v.price) }}</span>
            <span>数量 x{{ v.quantity }}</span>
          </div>
          <div class="item-side">
            <div class="line-total">￥{{ formatMoney(Number(v.price) * Number(v.quantity)) }}</div>
            <el-button size="small" text type="danger" @click="rem(k)">移除</el-button>
          </div>
        </article>
      </section>

      <aside class="section-shell summary">
        <div class="section-head">
          <div>
            <h2 class="section-title">结算概览</h2>
            <div class="section-copy">仅统计已勾选设备。</div>
          </div>
        </div>
        <div class="sum-row"><span>已选商品</span><strong>{{ selectedCount }}</strong></div>
        <div class="sum-row"><span>设备数量</span><strong>{{ selectedQuantity }}</strong></div>
        <div class="sum-row total"><span>预估金额</span><strong>￥{{ formatMoney(selectedAmount) }}</strong></div>
        <el-button type="primary" class="checkout-btn" @click="$router.push('/order/confirm')">前往确认订单</el-button>
      </aside>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { delCart, getCart, selectCart } from '@/api/oms'

const cart = ref({})
const ld = ref(true)

const entries = computed(() => Object.entries(cart.value || {}))
const selectedEntries = computed(() => entries.value.filter(([, value]) => value.selected))
const selectedCount = computed(() => selectedEntries.value.length)
const selectedQuantity = computed(() => selectedEntries.value.reduce((sum, [, value]) => sum + Number(value.quantity || 0), 0))
const selectedAmount = computed(() => selectedEntries.value.reduce((sum, [, value]) => sum + Number(value.price || 0) * Number(value.quantity || 0), 0))

onMounted(async () => {
  await load()
  ld.value = false
})

async function load() {
  const result = await getCart()
  cart.value = result.data || {}
}

async function rem(key) {
  await delCart(key)
  await load()
}

async function onSel(skuId, on) {
  await selectCart(skuId, !!on)
  await load()
}

function formatMoney(value) {
  const n = Number(value)
  return Number.isNaN(n) ? String(value ?? '--') : n.toFixed(2)
}

const SPEC_KEY_LABELS = {
  color: '颜色',
  edition: '版本',
  rom: '存储',
  memory: '内存',
  size: '尺寸',
}

function specText(value, key) {
  if (!value || value.specJson == null || value.specJson === '') {
    return '默认规格 SKU ' + key
  }
  let raw = value.specJson
  if (typeof raw === 'object' && !Array.isArray(raw)) {
    return renderSpecObj(raw)
  }
  if (typeof raw === 'string') {
    try {
      const parsed = JSON.parse(raw)
      if (parsed && typeof parsed === 'object') {
        return renderSpecObj(parsed)
      }
    } catch {
      return raw
    }
    return raw
  }
  return '默认规格 SKU ' + key
}

function renderSpecObj(obj) {
  const keys = Object.keys(obj)
  if (!keys.length) {
    return '默认规格'
  }
  return keys.map((k) => `${SPEC_KEY_LABELS[k] || k}：${obj[k]}`).join(' / ')
}
</script>

<style scoped lang="scss">
.ignore-vw.cart-page {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.ignore-vw .cart-layout {
  display: grid;
  grid-template-columns: minmax(0, 1.35fr) 360px;
  gap: 18px;
}

.ignore-vw .cart-item {
  display: grid;
  grid-template-columns: auto 76px 1fr auto;
  gap: 16px;
  align-items: center;
  padding: 16px 0;
  border-bottom: 1px solid $brand-border;
}

.ignore-vw .cart-item:last-child {
  border-bottom: none;
}

.ignore-vw .device-thumb {
  width: 76px;
  height: 76px;
  border-radius: 18px;
  display: grid;
  place-items: center;
  background: linear-gradient(135deg, $brand-primary, $brand-accent);
  color: #fff;
  font-size: 24px;
  font-weight: 800;
}

.ignore-vw .item-main {
  display: flex;
  flex-direction: column;
  gap: 7px;
}

.ignore-vw .item-main strong {
  color: $brand-ink;
  font-size: 16px;
}

.ignore-vw .item-main span {
  color: $text-sub;
  font-size: 13px;
}

.ignore-vw .item-side {
  text-align: right;
}

.ignore-vw .line-total {
  color: #ff5c43;
  font-size: 24px;
  font-weight: 800;
  margin-bottom: 8px;
}

.ignore-vw .sum-row {
  display: flex;
  justify-content: space-between;
  padding: 12px 0;
  color: $text-sub;
  border-bottom: 1px solid $brand-border;
}

.ignore-vw .sum-row strong {
  color: $brand-ink;
}

.ignore-vw .sum-row.total strong {
  color: #ff5c43;
  font-size: 24px;
  font-weight: 800;
}

.ignore-vw .checkout-btn {
  width: 100%;
  margin-top: 18px;
  height: 48px;
  border: none;
  border-radius: 999px;
  background: linear-gradient(135deg, $brand-primary, $brand-accent);
}

@media (max-width: 1080px) {
  .ignore-vw .cart-layout {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 760px) {
  .ignore-vw .cart-item {
    grid-template-columns: 1fr;
  }

  .ignore-vw .item-side {
    text-align: left;
  }
}
</style>
