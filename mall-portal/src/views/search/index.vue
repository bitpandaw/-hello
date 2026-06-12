<template>
  <div class="page search-page ignore-vw">
    <section class="section-shell search-hero">
      <div>
        <h1 class="section-title">精选设备检索</h1>
        <div class="section-copy">围绕数码产品销售平台的商品数据，支持关键字检索、分类筛选与价格排序。</div>
      </div>
      <div class="toolbar">
        <el-input v-model="q" placeholder="搜索手机、笔记本、平板、耳机等设备" @keyup.enter="load" clearable class="search-input">
          <template #append>
            <el-button @click="load">搜索设备</el-button>
          </template>
        </el-input>
        <el-radio-group v-model="sort" @change="load" class="sort-group">
          <el-radio-button label="create_time_desc">最新上架</el-radio-button>
          <el-radio-button label="price_asc">价格升序</el-radio-button>
          <el-radio-button label="price_desc">价格降序</el-radio-button>
        </el-radio-group>
      </div>
    </section>

    <el-skeleton v-if="ld" :rows="5" />

    <section v-else-if="list.length" class="result-grid">
      <article v-for="p in list" :key="p.id" class="result-card" @click="$router.push('/product/' + p.id)">
        <el-image :src="resolveProductImage(p.coverImg, p.name)" fit="cover" lazy class="thumb" />
        <div class="content">
          <div class="row-top">
            <span class="device-chip">{{ deviceType(p.name) }}</span>
            <span class="hint">商品库</span>
          </div>
          <div class="name">{{ p.name }}</div>
          <div class="desc">{{ productPitch(p.name) }}</div>
          <div class="price">￥{{ formatMoney(p.minPrice) }}</div>
        </div>
      </article>
    </section>

    <el-empty v-else description="未检索到匹配商品" class="section-shell empty-shell" />
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { productPage, search } from '@/api/pms'
import { resolveProductImage } from '@/utils/image'

const q = ref('')
const sort = ref('create_time_desc')
const list = ref([])
const ld = ref(true)
const route = useRoute()

onMounted(() => {
  if (route.query.q) {
    q.value = String(route.query.q)
  }
  load()
})

async function load() {
  ld.value = true
  try {
    if (q.value) {
      const result = await search(q.value)
      list.value = result.data || []
    } else {
      const cat = route.query.cat ? Number(route.query.cat) : undefined
      const result = await productPage({ p: 1, s: 20, categoryId: cat, sort: sort.value, q: undefined })
      list.value = result.data?.records || []
    }
  } finally {
    ld.value = false
  }
}

function formatMoney(value) {
  const n = Number(value)
  return Number.isNaN(n) ? String(value ?? '--') : n.toFixed(2)
}

function deviceType(name) {
  const text = String(name || '').toLowerCase()
  if (text.includes('phone')) return '智能手机'
  if (text.includes('book')) return '轻薄笔记本'
  if (text.includes('pad') || text.includes('display')) return '平板设备'
  if (text.includes('watch')) return '智能穿戴'
  if (text.includes('buds')) return '音频设备'
  if (text.includes('cam')) return '影像设备'
  if (text.includes('power')) return '数码配件'
  return '数码设备'
}

function productPitch(name) {
  const type = deviceType(name)
  const map = {
    智能手机: '聚焦移动影像、性能与日常通讯体验。',
    轻薄笔记本: '适合毕业设计开发、文档处理与演示汇报。',
    平板设备: '兼顾阅读记录、会议协同与娱乐展示。',
    智能穿戴: '强调健康监测、消息提醒与运动管理。',
    音频设备: '突出降噪、低延迟与便携式使用体验。',
    影像设备: '适合拍摄创作与内容展示场景。',
    数码配件: '补齐续航和外设扩展需求。',
  }
  return map[type] || '数码销售平台的设备展示信息。'
}
</script>

<style scoped lang="scss">
.ignore-vw.search-page {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.ignore-vw .search-hero {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.ignore-vw .toolbar {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.ignore-vw .search-input {
  max-width: 620px;
}

.ignore-vw .sort-group {
  width: fit-content;
}

.ignore-vw .result-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

.ignore-vw .result-card {
  @include panel;
  padding: 16px;
  display: flex;
  gap: 16px;
  cursor: pointer;
}

.ignore-vw .thumb {
  width: 180px;
  height: 180px;
  border-radius: 18px;
  background: linear-gradient(135deg, #eef4fa, #dfeaf6);
}

.ignore-vw .content {
  min-width: 0;
  display: flex;
  flex: 1;
  flex-direction: column;
}

.ignore-vw .row-top {
  display: flex;
  justify-content: space-between;
  gap: 10px;
  align-items: center;
}

.ignore-vw .hint {
  color: $text-soft;
  font-size: 12px;
}

.ignore-vw .name {
  margin-top: 12px;
  color: $brand-ink;
  font-size: 22px;
  font-weight: 800;
  line-height: 1.35;
}

.ignore-vw .desc {
  margin-top: 10px;
  color: $text-sub;
  font-size: 14px;
  line-height: 1.8;
}

.ignore-vw .price {
  margin-top: auto;
  color: #ff5c43;
  font-size: 28px;
  font-weight: 800;
}

@media (max-width: 960px) {
  .ignore-vw .result-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 760px) {
  .ignore-vw .result-card {
    flex-direction: column;
  }

  .ignore-vw .thumb {
    width: 100%;
    height: 240px;
  }
}
</style>
