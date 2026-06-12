<template>
  <div class="page product-detail ignore-vw">
    <el-skeleton v-if="ld" :rows="10" animated />
    <template v-else-if="d.product">
      <section class="hero">
        <div class="media-card">
          <div class="media-top">
            <span class="media-badge">数码优选</span>
            <span class="media-note">Spring Boot 数码产品销售平台</span>
          </div>
          <el-image :src="heroImage" fit="cover" class="hero-image" lazy />
        </div>

        <div class="summary-card">
          <div class="summary-top">
            <div>
              <div class="eyebrow">产品概览</div>
              <h1 class="title">{{ d.product.name }}</h1>
              <p v-if="d.product.subTitle" class="sub">{{ d.product.subTitle }}</p>
            </div>
            <div class="service-badges">
              <span v-for="tag in serviceTags" :key="tag" class="service-tag">{{ tag }}</span>
            </div>
          </div>

          <div class="price-panel">
            <div class="price-main">
              <span class="currency">￥</span>
              <strong>{{ currentPriceText }}</strong>
            </div>
            <div class="price-side">
              <span v-if="d.product.originalPrice" class="orig">￥{{ formatMoney(d.product.originalPrice) }}</span>
              <span class="save" v-if="saveAmountText">立省 {{ saveAmountText }}</span>
            </div>
          </div>

          <div class="feature-grid">
            <div v-for="item in highlights" :key="item.title" class="feature-item">
              <div class="feature-title">{{ item.title }}</div>
              <div class="feature-desc">{{ item.desc }}</div>
            </div>
          </div>

          <section v-if="d.skus && d.skus.length" class="spec-section">
            <div class="section-row">
            <h2>配置选择</h2>
            <span class="section-tip">请选择适合展示与下单的设备配置</span>
            </div>
            <div class="spec-grid" role="radiogroup" aria-label="商品规格">
              <button
                v-for="s in d.skus"
                :key="s.id"
                type="button"
                :class="['spec-card', { 'is-active': sku === s.id }]"
                @click="sku = s.id"
              >
                <span v-for="line in specLines(s)" :key="line" class="spec-line">{{ line }}</span>
              </button>
            </div>
          </section>

          <div class="action-row">
            <el-button type="primary" class="action-btn primary" :loading="adding" :disabled="!cur" @click="add">
              加入购物车
            </el-button>
            <el-button class="action-btn" @click="router.push('/cart')">查看购物车</el-button>
          </div>
        </div>
      </section>

      <section class="content-grid">
        <div class="detail-card">
          <div class="card-head">
            <h2>核心参数与产品介绍</h2>
            <span>适合毕业设计答辩与前台联调展示</span>
          </div>
          <div v-if="d.product.detailHtml" class="html" v-html="d.product.detailHtml" />
          <p v-else class="empty-text">暂无更多图文详情。</p>
        </div>

        <aside class="info-card">
          <div class="card-head">
            <h2>平台服务</h2>
            <span>数码销售平台保障说明</span>
          </div>
          <ul class="info-list">
            <li v-for="item in infoList" :key="item.title">
              <strong>{{ item.title }}</strong>
              <span>{{ item.desc }}</span>
            </li>
          </ul>
        </aside>
      </section>

      <section class="comment-section">
        <div class="card-head">
          <h2>商品评论</h2>
          <span>展示当前商品的公开评论列表</span>
        </div>
        <div v-if="commentLd" class="comment-loading">评论加载中...</div>
        <div v-else-if="comments.length" class="comment-list">
          <article v-for="item in comments" :key="item.id" class="comment-item">
            <div class="comment-head">
              <strong>{{ item.memberNick || '会员' }}</strong>
              <span>{{ scoreText(item.score) }}</span>
            </div>
            <p class="comment-body">{{ item.content || '该评论未填写文字内容。' }}</p>
            <time class="comment-time">{{ formatTime(item.createTime) }}</time>
          </article>
        </div>
        <p v-else class="comment-empty">当前商品暂时还没有公开评论。</p>
      </section>

    </template>
  </div>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { addCart } from '@/api/oms'
import { commentList, productDetail } from '@/api/pms'
import { useUserStore } from '@/stores/user'
import { resolveProductImage } from '@/utils/image'

const SPEC_KEY_LABELS = {
  color: '颜色',
  edition: '版本',
  rom: '存储',
  memory: '内存',
  size: '尺寸',
  spec: '规格',
}

const KEY_ORDER = ['color', 'edition', 'rom', 'memory', 'size', 'spec']

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const d = ref({ product: null, skus: [] })
const comments = ref([])
const ld = ref(true)
const commentLd = ref(false)
const sku = ref()
const adding = ref(false)

const serviceTags = ['官方正品', '闪电发货', '7天无忧', '平台售后']

const cur = computed(() => (d.value.skus || []).find((item) => item.id === sku.value))

const heroImage = computed(() => resolveProductImage(d.value.product?.coverImg, d.value.product?.name))

const currentPriceText = computed(() => {
  const value = cur.value?.price ?? d.value.product?.minPrice
  return value == null ? '--' : formatMoney(value)
})

const saveAmountText = computed(() => {
  const current = Number(cur.value?.price ?? d.value.product?.minPrice)
  const original = Number(d.value.product?.originalPrice)
  if (Number.isNaN(current) || Number.isNaN(original) || original <= current) {
    return ''
  }
  return `￥${formatMoney(original - current)}`
})

const highlights = computed(() => buildHighlights(d.value.product?.name))

const infoList = computed(() => [
  { title: '配送说明', desc: '支持全国主要地区发货，默认包邮。' },
  { title: '售后服务', desc: '支持订单查询、购物车联动与基础售后演示。' },
  { title: '适用场景', desc: '适合作为毕业设计商城系统的数码商品展示页。' },
  { title: '平台特点', desc: '前后端分离，商品、订单、会员与后台管理完整串联。' },
])

onMounted(loadDetail)

watch(
  () => route.params.id,
  () => loadDetail()
)

async function loadDetail() {
  ld.value = true
  try {
    const detailRes = await productDetail(route.params.id)
    d.value = detailRes.data || { product: null, skus: [] }
    sku.value = d.value.skus?.[0]?.id
    await loadComments()
  } finally {
    ld.value = false
  }
}

async function loadComments() {
  commentLd.value = true
  try {
    const result = await commentList({ productId: route.params.id, p: 1, s: 5 })
    comments.value = result.data?.records || []
  } catch {
    comments.value = []
  } finally {
    commentLd.value = false
  }
}

function formatMoney(value) {
  const amount = Number(value)
  if (Number.isNaN(amount)) {
    return String(value ?? '--')
  }
  return amount.toFixed(2)
}

function formatTime(value) {
  if (!value) {
    return '--'
  }
  return String(value).replace('T', ' ')
}

function scoreText(value) {
  const score = Number(value)
  if (Number.isNaN(score) || score <= 0) {
    return '未评分'
  }
  return `评分 ${score}/5`
}

function parseSpecJson(item) {
  const raw = item?.specJson
  if (!raw) {
    return {}
  }
  if (typeof raw === 'object' && !Array.isArray(raw)) {
    return raw
  }
  if (typeof raw === 'string') {
    try {
      const parsed = JSON.parse(raw)
      return parsed && typeof parsed === 'object' ? parsed : {}
    } catch {
      return { spec: raw }
    }
  }
  return {}
}

function orderedKeys(obj) {
  const keys = Object.keys(obj)
  const first = KEY_ORDER.filter((key) => keys.includes(key))
  const rest = keys.filter((key) => !KEY_ORDER.includes(key)).sort()
  return [...first, ...rest]
}

function specLines(item) {
  const data = parseSpecJson(item)
  const keys = orderedKeys(data)
  if (!keys.length) {
    return [`默认规格 #${item.id}`]
  }
  return keys.map((key) => `${SPEC_KEY_LABELS[key] || key}：${data[key]}`)
}

function buildHighlights(name) {
  const text = String(name || '').toLowerCase()
  if (text.includes('phone')) {
    return [
      { title: '旗舰性能', desc: '高性能芯片与高刷屏组合，兼顾流畅体验与续航。' },
      { title: '移动影像', desc: '适合日常拍摄、短视频记录和社交分享。' },
      { title: '通勤体验', desc: '轻薄机身，适合课堂、通勤和移动办公。' },
    ]
  }
  if (text.includes('book')) {
    return [
      { title: '轻薄办公', desc: '适合代码编写、文档处理和课程汇报。' },
      { title: '高效散热', desc: '长时间运行更稳定，适合毕业设计演示。' },
      { title: '高色域屏', desc: '日常观影、修图和界面展示更舒服。' },
    ]
  }
  if (text.includes('pad') || text.includes('display')) {
    return [
      { title: '高清大屏', desc: '更适合学习阅读、在线会议与图文展示。' },
      { title: '协同体验', desc: '适合多任务切换与轻办公场景。' },
      { title: '简洁桌面', desc: '外观简洁，符合现代数码产品视觉风格。' },
    ]
  }
  if (text.includes('watch')) {
    return [
      { title: '健康监测', desc: '支持运动与健康数据的日常记录。' },
      { title: '蓝牙通话', desc: '抬腕即可查看消息与来电提醒。' },
      { title: '轻量佩戴', desc: '适合通勤、运动和日常穿戴场景。' },
    ]
  }
  if (text.includes('buds')) {
    return [
      { title: '主动降噪', desc: '在通勤和学习环境中保持更沉浸的听感。' },
      { title: '稳定连接', desc: '低延迟连接，适合视频、音乐和轻游戏。' },
      { title: '长效续航', desc: '满足日常长时间佩戴和连续使用。' },
    ]
  }
  if (text.includes('cam')) {
    return [
      { title: '清晰成像', desc: '适合记录生活、旅行创作和课程素材采集。' },
      { title: '便携机身', desc: '外出携带更轻松，适合移动场景。' },
      { title: '上手友好', desc: '界面直观，适合项目展示与功能演示。' },
    ]
  }
  return [
    { title: '数码精选', desc: '围绕数码商城课题打造的演示商品信息。' },
    { title: '展示友好', desc: '适合前后端联调、论文截图和功能汇报。' },
    { title: '信息完整', desc: '包含价格、规格、库存与详情等核心模块。' },
  ]
}

async function add() {
  if (!cur.value) {
    return
  }
  if (!userStore.isLogin) {
    ElMessage.warning('请先登录后再加入购物车')
    router.push('/auth/login?redirect=' + encodeURIComponent(route.fullPath))
    return
  }
  adding.value = true
  try {
    await addCart(cur.value.id, 1)
    ElMessage.success('已加入购物车')
  } finally {
    adding.value = false
  }
}

</script>

<style scoped lang="scss">
.ignore-vw.product-detail {
  padding-bottom: 32px;
}

.ignore-vw .hero {
  display: grid;
  grid-template-columns: minmax(360px, 1.05fr) minmax(420px, 1fr);
  gap: 18px;
  align-items: start;
}

.ignore-vw .media-card,
.ignore-vw .summary-card,
.ignore-vw .detail-card,
.ignore-vw .info-card,
.ignore-vw .rec-section,
.ignore-vw .comment-section {
  background: #fff;
  border: 1px solid #eef1f5;
  border-radius: 22px;
  box-shadow: 0 12px 32px rgba(15, 23, 42, 0.06);
}

.ignore-vw .media-card {
  padding: 18px;
  background: linear-gradient(180deg, #fffaf6 0%, #ffffff 100%);
}

.ignore-vw .media-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 16px;
}

.ignore-vw .media-badge {
  background: #ffefe2;
  color: #f97316;
  border-radius: 999px;
  padding: 8px 14px;
  font-size: 13px;
  font-weight: 700;
}

.ignore-vw .media-note {
  color: #94a3b8;
  font-size: 13px;
}

.ignore-vw .hero-image {
  width: 100%;
  height: 470px;
  border-radius: 18px;
  background: linear-gradient(135deg, #f8fafc, #eef2ff);
}

.ignore-vw .summary-card {
  padding: 22px;
}

.ignore-vw .summary-top {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.ignore-vw .eyebrow {
  font-size: 13px;
  font-weight: 700;
  color: #f97316;
  letter-spacing: 0.06em;
  text-transform: uppercase;
}

.ignore-vw .title {
  margin: 8px 0 0;
  font-size: 34px;
  line-height: 1.18;
  font-weight: 800;
  color: #0f172a;
}

.ignore-vw .sub {
  margin: 14px 0 0;
  color: #64748b;
  font-size: 16px;
  line-height: 1.7;
}

.ignore-vw .service-badges {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.ignore-vw .service-tag {
  border: 1px solid #fde3cf;
  background: #fff7f1;
  color: #ea580c;
  border-radius: 999px;
  padding: 7px 12px;
  font-size: 12px;
  font-weight: 600;
}

.ignore-vw .price-panel {
  margin-top: 22px;
  padding: 20px 22px;
  border-radius: 20px;
  background: linear-gradient(135deg, #fff2e9, #fffaf5);
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  gap: 14px;
}

.ignore-vw .price-main {
  color: #ff4d4f;
  line-height: 1;
  display: flex;
  align-items: flex-end;
  gap: 6px;
}

.ignore-vw .currency {
  font-size: 28px;
  font-weight: 700;
  margin-bottom: 6px;
}

.ignore-vw .price-main strong {
  font-size: 60px;
  font-weight: 800;
  letter-spacing: -0.03em;
}

.ignore-vw .price-side {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 8px;
}

.ignore-vw .orig {
  color: #94a3b8;
  text-decoration: line-through;
  font-size: 22px;
}

.ignore-vw .save {
  color: #f97316;
  font-size: 14px;
  font-weight: 700;
}

.ignore-vw .feature-grid {
  margin-top: 20px;
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
}

.ignore-vw .feature-item {
  padding: 16px;
  border-radius: 16px;
  background: #f8fafc;
  border: 1px solid #edf2f7;
}

.ignore-vw .feature-title {
  font-size: 15px;
  font-weight: 700;
  color: #1e293b;
}

.ignore-vw .feature-desc {
  margin-top: 8px;
  color: #64748b;
  font-size: 13px;
  line-height: 1.7;
}

.ignore-vw .spec-section {
  margin-top: 22px;
}

.ignore-vw .section-row,
.ignore-vw .card-head {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
  gap: 14px;
}

.ignore-vw .section-row h2,
.ignore-vw .card-head h2 {
  margin: 0;
  font-size: 22px;
  font-weight: 800;
  color: #0f172a;
}

.ignore-vw .section-tip,
.ignore-vw .card-head span {
  color: #94a3b8;
  font-size: 13px;
}

.ignore-vw .spec-grid {
  margin-top: 14px;
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.ignore-vw .spec-card {
  text-align: left;
  padding: 16px;
  border-radius: 16px;
  border: 1px solid #e5e7eb;
  background: #fff;
  cursor: pointer;
  transition: transform 0.18s ease, border-color 0.18s ease, box-shadow 0.18s ease;
}

.ignore-vw .spec-card:hover {
  transform: translateY(-2px);
  border-color: #fdba74;
  box-shadow: 0 10px 24px rgba(249, 115, 22, 0.08);
}

.ignore-vw .spec-card.is-active {
  border-color: #f97316;
  background: linear-gradient(180deg, #fff8f2, #fff);
  box-shadow: 0 0 0 3px rgba(249, 115, 22, 0.08);
}

.ignore-vw .spec-line {
  display: block;
  color: #334155;
  font-size: 14px;
  line-height: 1.7;
}

.ignore-vw .spec-line + .spec-line {
  margin-top: 4px;
}

.ignore-vw .action-row {
  margin-top: 22px;
  display: flex;
  gap: 12px;
}

.ignore-vw .action-btn {
  flex: 1;
  height: 52px;
  border-radius: 999px;
  font-size: 16px;
  font-weight: 700;
}

.ignore-vw .action-btn.primary {
  background: linear-gradient(135deg, #ff8a00, #ff5a2f);
  border: none;
}

.ignore-vw .content-grid {
  margin-top: 20px;
  display: grid;
  grid-template-columns: minmax(0, 1.25fr) 320px;
  gap: 18px;
  align-items: start;
}

.ignore-vw .detail-card,
.ignore-vw .info-card,
.ignore-vw .rec-section,
.ignore-vw .comment-section {
  padding: 22px;
}

.ignore-vw .html,
.ignore-vw .empty-text {
  margin-top: 16px;
}

.ignore-vw .html {
  color: #475569;
  font-size: 15px;
  line-height: 1.85;
}

.ignore-vw .html :deep(h3) {
  margin: 0 0 12px;
  color: #0f172a;
  font-size: 22px;
}

.ignore-vw .html :deep(p) {
  margin: 0 0 12px;
}

.ignore-vw .html :deep(ul) {
  margin: 0;
  padding-left: 20px;
}

.ignore-vw .html :deep(li) {
  margin-bottom: 8px;
}

.ignore-vw .html :deep(img) {
  max-width: 100%;
  display: block;
  border-radius: 14px;
  margin: 16px 0;
}

.ignore-vw .empty-text {
  color: #94a3b8;
  font-size: 14px;
}

.ignore-vw .info-list {
  margin: 16px 0 0;
  padding: 0;
  list-style: none;
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.ignore-vw .info-list li {
  padding: 14px 0;
  border-bottom: 1px solid #eef2f7;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.ignore-vw .info-list li:last-child {
  border-bottom: none;
  padding-bottom: 0;
}

.ignore-vw .info-list strong {
  color: #0f172a;
  font-size: 15px;
}

.ignore-vw .info-list span {
  color: #64748b;
  font-size: 13px;
  line-height: 1.7;
}

.ignore-vw .rec-section {
  margin-top: 20px;
}

.ignore-vw .comment-section {
  margin-top: 20px;
}

.ignore-vw .comment-loading,
.ignore-vw .comment-empty {
  margin-top: 16px;
  color: #94a3b8;
  font-size: 14px;
}

.ignore-vw .comment-list {
  margin-top: 16px;
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.ignore-vw .comment-item {
  padding: 16px 0;
  border-bottom: 1px solid #eef2f7;
}

.ignore-vw .comment-item:last-child {
  border-bottom: none;
  padding-bottom: 0;
}

.ignore-vw .comment-head {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: center;
}

.ignore-vw .comment-head strong {
  color: #0f172a;
  font-size: 15px;
}

.ignore-vw .comment-head span {
  color: #f97316;
  font-size: 13px;
  font-weight: 700;
}

.ignore-vw .comment-body {
  margin: 10px 0 0;
  color: #475569;
  font-size: 14px;
  line-height: 1.8;
}

.ignore-vw .comment-time {
  display: block;
  margin-top: 8px;
  color: #94a3b8;
  font-size: 12px;
}

.ignore-vw .rec-grid {
  margin-top: 16px;
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 14px;
}

.ignore-vw .rec-item {
  border-radius: 18px;
  background: #f8fafc;
  border: 1px solid #edf2f7;
  overflow: hidden;
  cursor: pointer;
  transition: transform 0.18s ease, box-shadow 0.18s ease;
}

.ignore-vw .rec-item:hover {
  transform: translateY(-3px);
  box-shadow: 0 12px 24px rgba(15, 23, 42, 0.08);
}

.ignore-vw .rec-img {
  width: 100%;
  height: 180px;
  background: linear-gradient(135deg, #f8fafc, #eef2ff);
}

.ignore-vw .rec-body {
  padding: 14px;
}

.ignore-vw .rec-name {
  color: #0f172a;
  font-size: 15px;
  line-height: 1.55;
  min-height: 46px;
}

.ignore-vw .rec-price {
  margin-top: 10px;
  color: #ff4d4f;
  font-size: 22px;
  font-weight: 800;
}

@media (max-width: 1180px) {
  .ignore-vw .hero,
  .ignore-vw .content-grid {
    grid-template-columns: 1fr;
  }

  .ignore-vw .feature-grid,
  .ignore-vw .rec-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 780px) {
  .ignore-vw .hero-image {
    height: 320px;
  }

  .ignore-vw .title {
    font-size: 28px;
  }

  .ignore-vw .price-main strong {
    font-size: 44px;
  }

  .ignore-vw .price-panel,
  .ignore-vw .action-row,
  .ignore-vw .section-row,
  .ignore-vw .card-head {
    flex-direction: column;
    align-items: flex-start;
  }

  .ignore-vw .feature-grid,
  .ignore-vw .spec-grid,
  .ignore-vw .rec-grid {
    grid-template-columns: 1fr;
  }
}
</style>
