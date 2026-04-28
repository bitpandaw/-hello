<template>
  <div class="page">
    <el-skeleton v-if="ld" :rows="4" />
    <el-empty v-else-if="!Object.keys(cart).length" description="购物车是空的" />
    <el-card v-else v-for="(v, k) in cart" :key="k" class="c">
      <el-checkbox :model-value="v.selected" @change="(on) => onSel(k, on)">
        <div class="item-main">
          <div class="spec">{{ specText(v, k) }}</div>
          <div class="meta">x{{ v.quantity }} — ￥{{ v.price }} / 件</div>
        </div>
      </el-checkbox>
      <el-button size="small" type="danger" text @click="rem(k)">删</el-button>
    </el-card>
    <el-button
      v-if="Object.keys(cart).length"
      type="primary"
      @click="$router.push('/order/confirm')"
      style="width: 100%; margin-top: 0.2rem"
      >去结算</el-button
    >
    <el-card v-if="recs.length" class="rec-card">
      <template #header>凑单推荐</template>
      <div class="rec-grid">
        <div v-for="(p, idx) in recs" :key="p.id" class="rec-item" @click="goRec(p, idx)">
          <el-image :src="p.coverImg || 'https://via.placeholder.com/200'" fit="cover" class="rec-img" />
          <div class="rec-name">{{ p.name }}</div>
          <div class="rec-price">￥{{ p.minPrice }}</div>
        </div>
      </div>
    </el-card>
  </div>
</template>
<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getCart, delCart, selectCart } from '@/api/oms'
import { guessRecommend, reportRecommendExpose, reportRecommendClick } from '@/api/pms'
const cart = ref({})
const recs = ref([])
const recRequestId = ref('')
const ld = ref(true)
const router = useRouter()

onMounted(async () => {
  await load()
  await loadRecs()
  ld.value = false
})
async function load() {
  const t = await getCart()
  cart.value = t.data || {}
}
async function rem(k) {
  await delCart(k)
  await load()
}
async function onSel(skuId, on) {
  await selectCart(skuId, !!on)
  await load()
}

const SPEC_KEY_LABELS = {
  color: '颜色',
  edition: '版本',
  rom: '存储',
  memory: '内存',
  size: '尺码',
}

function specText(v, k) {
  if (!v || v.specJson == null || v.specJson === '') {
    return 'SKU ' + k
  }
  let raw = v.specJson
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
  return 'SKU ' + k
}

function renderSpecObj(obj) {
  const keys = Object.keys(obj)
  if (!keys.length) {
    return '默认规格'
  }
  return keys.map((k) => `${SPEC_KEY_LABELS[k] || k}：${obj[k]}`).join(' / ')
}

async function loadRecs() {
  const t = await guessRecommend({ size: 4 })
  recRequestId.value = t.data?.requestId || ''
  recs.value = t.data?.products || []
  recs.value.forEach((x, idx) => {
    reportRecommendExpose({
      scene: 'cart_bundle',
      requestId: recRequestId.value,
      itemId: x.id,
      position: idx + 1,
    })
  })
}

function goRec(p, idx) {
  if (recRequestId.value) {
    reportRecommendClick({
      scene: 'cart_bundle',
      requestId: recRequestId.value,
      itemId: p.id,
      position: idx + 1,
    })
  }
  router.push('/product/' + p.id)
}
</script>
<style scoped>
.c {
  margin-bottom: 0.16rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.item-main {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.spec {
  color: #303133;
}

.meta {
  color: #909399;
  font-size: 13px;
}

.rec-card {
  margin-top: 12px;
}

.rec-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
}

.rec-item {
  cursor: pointer;
}

.rec-img {
  width: 100%;
  height: 110px;
  border-radius: 6px;
}

.rec-name {
  margin-top: 6px;
  min-height: 36px;
  font-size: 13px;
}

.rec-price {
  color: #ff4d4f;
  margin-top: 4px;
}
</style>
