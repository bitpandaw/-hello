<template>
  <div class="page product-detail ignore-vw">
    <el-skeleton v-if="ld" :rows="8" />
    <template v-else-if="d.product">
      <el-carousel
        v-if="imgs.length"
        height="380px"
        indicator-position="outside"
        :interval="5000"
        class="gallery"
      >
        <el-carousel-item v-for="(x, i) in imgs" :key="i">
          <el-image :src="x" fit="cover" class="cover" lazy />
        </el-carousel-item>
      </el-carousel>
      <div v-else class="cover-placeholder">暂无主图</div>

      <section class="head-block">
        <h1 class="title">{{ d.product.name }}</h1>
        <p v-if="d.product.subTitle" class="sub">{{ d.product.subTitle }}</p>
        <div class="price-row">
          <span class="cur-price"
            >￥<strong>{{ currentPriceText }}</strong></span
          >
          <span v-if="d.product.originalPrice" class="orig"
            >￥{{ formatMoney(d.product.originalPrice) }}</span
          >
        </div>
      </section>

      <section v-if="d.skus && d.skus.length" class="spec-section">
        <div class="spec-hd">选择规格</div>
        <div class="spec-chips" role="radiogroup" aria-label="商品规格">
          <button
            v-for="s in d.skus"
            :key="s.id"
            type="button"
            :class="['spec-chip', { 'is-active': sku === s.id }]"
            @click="sku = s.id"
          >
            <span v-for="line in specLines(s)" :key="line" class="spec-line">{{ line }}</span>
          </button>
        </div>
        <p v-if="!cur" class="hint">请选择规格</p>
      </section>

      <el-button
        type="primary"
        class="add"
        :loading="adding"
        :disabled="!cur"
        @click="add"
        >加入购物车</el-button
      >

      <section v-if="d.product.detailHtml" class="detail-section">
        <h2 class="detail-hd">商品详情</h2>
        <div class="html" v-html="d.product.detailHtml" />
      </section>
      <p v-else class="no-detail">暂无更多详情</p>

      <section v-if="recs.length" class="rec-section">
        <h2 class="detail-hd">看了又看</h2>
        <div class="rec-grid">
          <div
            v-for="(p, idx) in recs"
            :key="p.id"
            class="rec-item"
            @click="goRec(p, idx)"
          >
            <el-image :src="p.coverImg || 'https://via.placeholder.com/200'" fit="cover" class="rec-img" />
            <div class="rec-name">{{ p.name }}</div>
            <div class="rec-price">￥{{ formatMoney(p.minPrice) }}</div>
          </div>
        </div>
      </section>
    </template>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { productDetail, similarRecommend, reportRecommendExpose, reportRecommendClick } from '@/api/pms'
import { addCart } from '@/api/oms'
import { ElMessage } from 'element-plus'

const SPEC_KEY_LABELS = {
  color: '颜色',
  edition: '版本',
  rom: '存储',
  memory: '内存',
  size: '尺码',
  spec: '规格',
}

const KEY_ORDER = ['color', 'edition', 'rom', 'memory', 'size', 'spec']

const route = useRoute()
const router = useRouter()
const d = ref({ product: null, skus: [] })
const recs = ref([])
const recRequestId = ref('')
const ld = ref(true)
const sku = ref()
const adding = ref(false)

const cur = computed(() => (d.value.skus || []).find((x) => x.id === sku.value))

const imgs = computed(() => {
  const p = d.value.product
  if (!p) {
    return []
  }
  if (p.coverImg) {
    return [p.coverImg]
  }
  return []
})

const currentPriceText = computed(() => {
  if (cur.value && cur.value.price != null) {
    return formatMoney(cur.value.price)
  }
  if (d.value.product && d.value.product.minPrice != null) {
    return formatMoney(d.value.product.minPrice)
  }
  return '—'
})

onMounted(loadDetail)
watch(
  () => route.params.id,
  () => loadDetail()
)

async function loadDetail() {
  ld.value = true
  const t = await productDetail(route.params.id)
  d.value = t.data || { product: null, skus: [] }
  if (d.value.skus && d.value.skus[0]) {
    sku.value = d.value.skus[0].id
  }
  const r = await similarRecommend({ itemId: route.params.id, size: 6 })
  recRequestId.value = r.data?.requestId || ''
  recs.value = r.data?.products || []
  recs.value.forEach((x, idx) => {
    reportRecommendExpose({
      scene: 'product_similar',
      requestId: recRequestId.value,
      itemId: x.id,
      position: idx + 1,
    })
  })
  ld.value = false
}

function formatMoney(v) {
  const n = Number(v)
  if (Number.isNaN(n)) {
    return String(v)
  }
  return n.toFixed(2)
}

function parseSpecJson(s) {
  if (!s || s.specJson == null) {
    return {}
  }
  const raw = s.specJson
  if (typeof raw === 'object' && !Array.isArray(raw)) {
    return raw
  }
  if (typeof raw === 'string') {
    try {
      const o = JSON.parse(raw)
      if (o && typeof o === 'object') {
        return o
      }
    } catch {
      return { 规格: raw }
    }
  }
  return {}
}

function specKeyLabel(k) {
  return SPEC_KEY_LABELS[k] || k
}

function orderedKeys(obj) {
  const keys = Object.keys(obj)
  const first = KEY_ORDER.filter((k) => keys.includes(k))
  const rest = keys.filter((k) => !KEY_ORDER.includes(k)).sort()
  return [...first, ...rest]
}

function specLines(s) {
  const o = parseSpecJson(s)
  const keys = orderedKeys(o)
  if (!keys.length) {
    return ['规格 ' + s.id]
  }
  return keys.map((k) => `${specKeyLabel(k)}：${o[k]}`)
}

async function add() {
  if (!cur.value) {
    return
  }
  adding.value = true
  try {
    await addCart(cur.value.id, 1)
    ElMessage.success('已加购')
  } finally {
    adding.value = false
  }
}

function goRec(p, idx) {
  if (recRequestId.value) {
    reportRecommendClick({
      scene: 'product_similar',
      requestId: recRequestId.value,
      itemId: p.id,
      position: idx + 1,
    })
  }
  router.push('/product/' + p.id)
}
</script>

<style scoped lang="scss">
.product-detail {
  max-width: 800px;
}

.gallery {
  border-radius: 12px;
  overflow: hidden;
  background: #fff;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  margin-bottom: 20px;
}

.cover {
  width: 100%;
  height: 380px;
  display: block;
}

.cover-placeholder {
  height: 220px;
  border-radius: 12px;
  background: #ebeef5;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #909399;
  margin-bottom: 20px;
}

.head-block {
  margin-bottom: 16px;
}

.title {
  font-size: 20px;
  line-height: 1.4;
  font-weight: 700;
  color: #303133;
  margin: 0 0 8px;
}

.sub {
  margin: 0 0 12px;
  font-size: 14px;
  color: #909399;
  line-height: 1.5;
}

.price-row {
  display: flex;
  align-items: baseline;
  gap: 12px;
}

.cur-price {
  color: #ff4d4f;
  font-size: 16px;
  strong {
    font-size: 28px;
    font-weight: 800;
    letter-spacing: -0.5px;
  }
}

.orig {
  font-size: 14px;
  color: #c0c4cc;
  text-decoration: line-through;
}

.spec-section {
  background: #fff;
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 16px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
}

.spec-hd {
  font-size: 15px;
  font-weight: 600;
  margin-bottom: 12px;
  color: #303133;
}

.spec-chips {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.spec-chip {
  text-align: left;
  width: 100%;
  border: 1px solid #dcdfe6;
  background: #fafafa;
  border-radius: 10px;
  padding: 10px 14px;
  cursor: pointer;
  font-size: 14px;
  color: #606266;
  line-height: 1.5;
  transition: border-color 0.2s, background 0.2s, color 0.2s;
}

.spec-chip:hover {
  border-color: #ff6a00;
  background: #fff7f0;
}

.spec-chip.is-active {
  border-color: #ff6a00;
  background: #fff4eb;
  color: #d35400;
  font-weight: 500;
  box-shadow: 0 0 0 1px rgba(255, 106, 0, 0.2);
}

.spec-line {
  display: block;
}

.spec-line + .spec-line {
  margin-top: 4px;
}

.hint {
  font-size: 12px;
  color: #f56c6c;
  margin: 8px 0 0;
}

.add {
  width: 100%;
  height: 48px;
  font-size: 16px;
  font-weight: 600;
  border-radius: 24px;
  margin-bottom: 24px;
}

.detail-section {
  background: #fff;
  border-radius: 12px;
  padding: 16px;
  overflow: hidden;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.05);
}

.detail-hd {
  font-size: 16px;
  font-weight: 600;
  margin: 0 0 12px;
  color: #303133;
  border-left: 4px solid #ff6a00;
  padding-left: 10px;
}

.html {
  font-size: 14px;
  line-height: 1.6;
  color: #606266;
  :deep(img) {
    max-width: 100%;
    height: auto;
    display: block;
    margin: 8px 0;
    border-radius: 6px;
  }
  :deep(p) {
    margin: 0.5em 0;
  }
}

.no-detail {
  text-align: center;
  color: #909399;
  padding: 24px;
}

.rec-section {
  margin-top: 18px;
  background: #fff;
  border-radius: 12px;
  padding: 16px;
}

.rec-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
}

.rec-item {
  cursor: pointer;
}

.rec-img {
  width: 100%;
  height: 120px;
  border-radius: 8px;
}

.rec-name {
  margin-top: 6px;
  font-size: 13px;
  line-height: 1.4;
  min-height: 36px;
}

.rec-price {
  margin-top: 6px;
  color: #ff4d4f;
  font-weight: 700;
}
</style>
