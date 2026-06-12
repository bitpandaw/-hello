<template>
  <div class="page home ignore-vw">
    <el-skeleton v-if="load" :rows="8" animated class="skeleton" />
    <template v-else>
      <section class="hero-shell">
        <div class="hero-copy">
          <span class="hero-tag">毕业设计展示项目</span>
          <h1>Spring Boot 数码销售平台</h1>
          <p>
            面向数码产品销售场景构建的前后端分离系统，涵盖商品浏览、分类检索、
            购物车结算、订单处理与后台管理，适合课程答辩与项目成果展示。
          </p>
          <div class="hero-actions">
            <el-button type="primary" size="large" @click="$router.push('/search')">进入商品库</el-button>
            <el-button size="large" @click="$router.push('/category')">查看数码分类</el-button>
          </div>
          <div class="hero-points">
            <div class="point">
              <strong>商品模块</strong>
              <span>支持分类、检索、详情与图片展示</span>
            </div>
            <div class="point">
              <strong>订单链路</strong>
              <span>支持购物车、确认订单与模拟支付流程</span>
            </div>
            <div class="point">
              <strong>后台管理</strong>
              <span>支持商品、订单、会员与权限管理</span>
            </div>
          </div>
        </div>

        <div class="hero-device">
          <div class="device-frame">
            <img src="/real-products/laptop.jpg" alt="数码设备展示" class="device-image" />
          </div>
          <div class="device-float spec-a">
            <span>高性能设备</span>
            <strong>商品、订单、会员联动</strong>
          </div>
          <div class="device-float spec-b">
            <span>项目特性</span>
            <strong>前后端分离与真实商品图接入</strong>
          </div>
        </div>
      </section>

      <section class="section-shell category-shell">
        <div class="section-head">
          <div>
            <h2 class="section-title">核心品类</h2>
            <div class="section-copy">围绕数码产品销售平台的主要展示设备，覆盖移动终端、办公设备与智能配件。</div>
          </div>
          <el-link type="primary" @click="$router.push('/category')">查看全部分类</el-link>
        </div>
        <div class="category-grid">
          <button
            v-for="item in categoryShowcase"
            :key="item.label"
            class="category-card"
            @click="goCategory(item)"
          >
            <span class="device-chip">{{ item.badge }}</span>
            <strong>{{ item.label }}</strong>
            <p>{{ item.copy }}</p>
          </button>
        </div>
      </section>

      <section class="section-shell">
        <div class="section-head">
          <div>
            <h2 class="section-title">精选设备</h2>
            <div class="section-copy">基于当前商品数据生成的展示设备列表，突出图片、价格与关键规格信息。</div>
          </div>
          <el-link type="primary" @click="$router.push('/search')">进入商品库</el-link>
        </div>
        <div class="product-grid">
          <article
            v-for="(p, idx) in products"
            :key="p.id"
            class="device-card"
            @click="goDetail(p, idx)"
          >
            <div class="thumb-wrap">
              <el-image :src="resolveProductImage(p.coverImg, p.name)" fit="cover" lazy class="goods-img" />
            </div>
            <div class="card-body">
              <div class="card-top">
                <span class="device-chip">{{ deviceType(p.name) }}</span>
                <span class="device-meta">平台精选</span>
              </div>
              <h3 class="pname">{{ p.name }}</h3>
              <p class="desc">{{ productPitch(p) }}</p>
              <div class="spec-list">
                <span v-for="chip in specChips(p.name)" :key="chip" class="spec-chip">{{ chip }}</span>
              </div>
              <div class="meta">
                <div class="price">￥{{ formatMoney(p.minPrice) }}</div>
                <span class="cta">查看详情</span>
              </div>
            </div>
          </article>
        </div>
      </section>

      <section class="section-shell ability-shell">
        <div class="section-head">
          <div>
            <h2 class="section-title">平台能力</h2>
            <div class="section-copy">展示毕业设计中前台系统的关键能力模块与业务链路完整性。</div>
          </div>
        </div>
        <div class="ability-grid">
          <div v-for="item in abilities" :key="item.title" class="ability-card">
            <strong>{{ item.title }}</strong>
            <p>{{ item.copy }}</p>
          </div>
        </div>
      </section>
    </template>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { categoryTree, productPage } from '@/api/pms'
import { resolveProductImage } from '@/utils/image'

const load = ref(true)
const router = useRouter()
const cats = ref([])
const products = ref([])

const abilities = [
  { title: '商品展示', copy: '支持多类别商品展示、实拍图片接入与详情页参数展示。' },
  { title: '搜索分类', copy: '支持按关键字、分类与排序条件进行数码设备筛选。' },
  { title: '订单流程', copy: '包含购物车、确认订单、模拟支付与订单详情查看。' },
  { title: '后台管理', copy: '支持商品、订单、会员与权限等管理端功能。' },
]

const categoryShowcase = computed(() => {
  const top = cats.value.slice(0, 6)
  const defaults = [
    { label: '智能手机', badge: '移动终端', copy: '聚焦日常通讯、影像与移动办公场景。', queryId: null },
    { label: '轻薄笔记本', badge: '办公设备', copy: '适合课程设计、开发调试与文档处理。', queryId: null },
    { label: '平板设备', badge: '学习娱乐', copy: '兼顾在线会议、阅读记录与影音使用。', queryId: null },
    { label: '智能穿戴', badge: '健康互联', copy: '面向手表与穿戴设备的展示与选购。', queryId: null },
    { label: '音频设备', badge: '沉浸体验', copy: '展示耳机、音频配件等数码周边产品。', queryId: null },
    { label: '影像与配件', badge: '内容创作', copy: '涵盖相机设备、储能配件与创作场景。', queryId: null },
  ]

  return defaults.map((item, index) => ({
    ...item,
    queryId: top[index]?.id || null,
    label: top[index]?.name || item.label,
  }))
})

onMounted(async () => {
  try {
    const treeRes = await categoryTree()
    cats.value = treeRes.data || []
    const productRes = await productPage({ p: 1, s: 8 })
    products.value = productRes.data?.records || []
  } finally {
    load.value = false
  }
})

function goCategory(item) {
  router.push(item.queryId ? `/search?cat=${item.queryId}` : '/category')
}

function goDetail(item) {
  router.push('/product/' + item.id)
}

function formatMoney(value) {
  const n = Number(value)
  return Number.isNaN(n) ? String(value ?? '--') : n.toFixed(2)
}

function deviceType(name) {
  const text = String(name || '').toLowerCase()
  if (text.includes('phone')) return '智能手机'
  if (text.includes('book') || text.includes('laptop')) return '笔记本'
  if (text.includes('pad') || text.includes('tablet') || text.includes('display')) return '平板设备'
  if (text.includes('watch')) return '智能穿戴'
  if (text.includes('buds') || text.includes('ear')) return '音频设备'
  if (text.includes('cam')) return '影像设备'
  if (text.includes('power')) return '数码配件'
  return '数码设备'
}

function productPitch(item) {
  const type = deviceType(item.name)
  const map = {
    智能手机: '高刷屏与移动影像结合，适合日常使用与演示展示。',
    笔记本: '轻薄便携与高效办公结合，适合作为系统展示终端。',
    平板设备: '大屏阅读与协同体验兼顾，适合学习与会议场景。',
    智能穿戴: '聚焦健康监测与消息提醒，突出智能互联体验。',
    音频设备: '强调沉浸式听感、稳定连接与便携佩戴体验。',
    影像设备: '面向内容创作与拍摄记录，突出成像与便携表现。',
    数码配件: '提供续航与扩展支持，完善整套数码使用场景。',
  }
  return map[type] || '围绕数码销售平台课题构建的展示型设备信息。'
}

function specChips(name) {
  const type = deviceType(name)
  const map = {
    智能手机: ['高刷屏', '移动影像', '5G连接'],
    笔记本: ['轻薄机身', '高色域', '长续航'],
    平板设备: ['大屏显示', '多任务', '学习娱乐'],
    智能穿戴: ['健康监测', '蓝牙通话', '运动模式'],
    音频设备: ['主动降噪', '低延迟', '长效续航'],
    影像设备: ['清晰成像', '轻量便携', '创作记录'],
    数码配件: ['快充支持', '多设备兼容', '便携补能'],
  }
  return map[type] || ['真实商品图', '系统展示', '数码平台']
}
</script>

<style scoped lang="scss">
.ignore-vw.home {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.ignore-vw .hero-shell {
  @include panel;
  padding: 26px;
  display: grid;
  grid-template-columns: minmax(0, 1.15fr) minmax(360px, 0.95fr);
  gap: 24px;
  background:
    radial-gradient(circle at top right, rgba(0, 184, 217, 0.14), transparent 30%),
    linear-gradient(135deg, rgba(31, 107, 255, 0.04), rgba(255, 123, 47, 0.04)),
    #fff;
}

.ignore-vw .hero-tag {
  display: none;
}

.ignore-vw .hero-copy h1 {
  margin: 18px 0 0;
  font-size: 52px;
  line-height: 1.08;
  color: $brand-ink;
  letter-spacing: -0.03em;
}

.ignore-vw .hero-copy p {
  margin: 18px 0 0;
  max-width: 660px;
  color: $text-sub;
  font-size: 16px;
  line-height: 1.9;
}

.ignore-vw .hero-actions {
  margin-top: 24px;
  display: flex;
  gap: 12px;
}

.ignore-vw .hero-actions :deep(.el-button--primary) {
  border: none;
  background: linear-gradient(135deg, $brand-primary, $brand-accent);
}

.ignore-vw .hero-points {
  margin-top: 24px;
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
}

.ignore-vw .point {
  padding: 16px;
  border-radius: 18px;
  background: rgba(248, 251, 255, 0.92);
  border: 1px solid rgba(217, 227, 238, 0.8);
}

.ignore-vw .point strong {
  display: block;
  color: $brand-ink;
  font-size: 15px;
  font-weight: 800;
}

.ignore-vw .point span {
  display: block;
  margin-top: 8px;
  color: $text-sub;
  font-size: 13px;
  line-height: 1.7;
}

.ignore-vw .hero-device {
  position: relative;
  min-height: 100%;
}

.ignore-vw .device-frame {
  height: 100%;
  min-height: 420px;
  border-radius: 28px;
  overflow: hidden;
  background: linear-gradient(180deg, #dce9f7, #f6fbff);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.7);
}

.ignore-vw .device-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.ignore-vw .device-float {
  position: absolute;
  padding: 14px 16px;
  border-radius: 18px;
  background: rgba(8, 17, 32, 0.82);
  color: #fff;
  backdrop-filter: blur(12px);
  max-width: 240px;
  box-shadow: 0 22px 34px rgba(8, 17, 32, 0.22);
}

.ignore-vw .device-float span {
  display: block;
  color: rgba(255, 255, 255, 0.7);
  font-size: 12px;
}

.ignore-vw .device-float strong {
  display: block;
  margin-top: 6px;
  font-size: 15px;
  line-height: 1.5;
}

.ignore-vw .spec-a {
  left: -10px;
  bottom: 26px;
}

.ignore-vw .spec-b {
  right: -6px;
  top: 28px;
}

.ignore-vw .category-grid,
.ignore-vw .ability-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 14px;
}

.ignore-vw .category-card,
.ignore-vw .ability-card {
  padding: 18px;
  border-radius: 18px;
  border: 1px solid $brand-border;
  background: linear-gradient(180deg, #ffffff, #f8fbff);
  text-align: left;
}

.ignore-vw .category-card {
  cursor: pointer;
  transition: transform 0.18s ease, box-shadow 0.18s ease;
}

.ignore-vw .category-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 18px 28px rgba(7, 16, 31, 0.08);
}

.ignore-vw .category-card strong,
.ignore-vw .ability-card strong {
  display: block;
  margin-top: 12px;
  color: $brand-ink;
  font-size: 18px;
}

.ignore-vw .category-card p,
.ignore-vw .ability-card p {
  margin: 10px 0 0;
  color: $text-sub;
  font-size: 14px;
  line-height: 1.8;
}

.ignore-vw .product-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
}

.ignore-vw .device-card {
  border-radius: 22px;
  border: 1px solid $brand-border;
  background: linear-gradient(180deg, #ffffff, #f8fbff);
  overflow: hidden;
  cursor: pointer;
  transition: transform 0.18s ease, box-shadow 0.18s ease;
}

.ignore-vw .device-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 22px 32px rgba(7, 16, 31, 0.1);
}

.ignore-vw .thumb-wrap {
  padding: 18px 18px 0;
}

.ignore-vw .goods-img {
  width: 100%;
  height: 230px;
  border-radius: 18px;
  background: linear-gradient(135deg, #eef4fa, #dce7f4);
}

.ignore-vw .card-body {
  padding: 18px;
}

.ignore-vw .card-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
}

.ignore-vw .device-meta {
  color: $text-soft;
  font-size: 12px;
}

.ignore-vw .pname {
  margin: 14px 0 0;
  min-height: 52px;
  color: $brand-ink;
  font-size: 20px;
  line-height: 1.35;
}

.ignore-vw .desc {
  margin: 10px 0 0;
  min-height: 72px;
  color: $text-sub;
  font-size: 13px;
  line-height: 1.8;
}

.ignore-vw .spec-list {
  margin-top: 12px;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.ignore-vw .spec-chip {
  display: inline-flex;
  align-items: center;
  padding: 6px 10px;
  border-radius: 999px;
  background: $brand-surface-muted;
  color: $text-sub;
  font-size: 12px;
  font-weight: 700;
}

.ignore-vw .meta {
  margin-top: 16px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
}

.ignore-vw .price {
  color: #ff5c43;
  font-size: 26px;
  font-weight: 800;
}

.ignore-vw .cta {
  color: $brand-primary;
  font-size: 13px;
  font-weight: 700;
}

@media (max-width: 1180px) {
  .ignore-vw .hero-shell {
    grid-template-columns: 1fr;
  }

  .ignore-vw .product-grid {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}

@media (max-width: 920px) {
  .ignore-vw .hero-points,
  .ignore-vw .category-grid,
  .ignore-vw .ability-grid {
    grid-template-columns: 1fr;
  }

  .ignore-vw .product-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 760px) {
  .ignore-vw .hero-shell {
    padding: 18px;
  }

  .ignore-vw .hero-copy h1 {
    font-size: 34px;
  }

  .ignore-vw .hero-actions {
    flex-direction: column;
    align-items: stretch;
  }

  .ignore-vw .device-frame {
    min-height: 280px;
  }

  .ignore-vw .spec-b,
  .ignore-vw .spec-a {
    position: static;
    margin-top: 12px;
    max-width: none;
  }

  .ignore-vw .product-grid {
    grid-template-columns: 1fr;
  }
}
</style>
