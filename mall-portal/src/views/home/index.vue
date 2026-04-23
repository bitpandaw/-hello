<template>
  <div class="page home">
    <el-skeleton v-if="load" :rows="6" animated class="skeleton" />
    <template v-else>
      <swiper
        v-if="banners.length"
        :modules="[Pagination, Autoplay]"
        :pagination="{ clickable: true }"
        :autoplay="{ delay: 3500 }"
        class="banner"
      >
        <swiper-slide v-for="(b, i) in banners" :key="i"
          ><div class="bn">{{ b }}</div></swiper-slide
        >
      </swiper>
      <div class="navs">
        <el-button v-for="c in cats.slice(0, 4)" :key="c.id" @click="$router.push('/search?cat=' + c.id)">{{
          c.name
        }}</el-button>
      </div>
      <h3>推荐</h3>
      <el-row :gutter="8">
        <el-col v-for="p in products" :key="p.id" :span="12" class="card" @click="$router.push('/product/' + p.id)">
          <el-card shadow="hover">
            <el-image :src="p.coverImg || 'https://via.placeholder.com/200'" fit="cover" lazy class="img-lazy" />
            <div class="pname">{{ p.name }}</div>
            <div class="price">￥{{ p.minPrice }}</div>
          </el-card>
        </el-col>
      </el-row>
    </template>
    <div class="tabbar">
      <router-link to="/home">首页</router-link>
      <router-link to="/category">分类</router-link>
      <router-link to="/cart">购物车</router-link>
      <router-link to="/member">我的</router-link>
    </div>
  </div>
</template>
<script setup>
import { ref, onMounted } from 'vue'
import { Swiper, SwiperSlide } from 'swiper/vue'
import { Pagination, Autoplay } from 'swiper/modules'
import { categoryTree, productPage } from '@/api/pms'
import 'swiper/css'
import 'swiper/css/pagination'

const load = ref(true)
const cats = ref([])
const products = ref({ records: [] })
const banners = ref(['Mall 毕设 Banner-1', 'Mall 毕设 Banner-2', 'Mall 毕设 Banner-3'])
onMounted(async () => {
  try {
    const t = await categoryTree()
    cats.value = t.data || []
    const g = await productPage({ p: 1, s: 8 })
    products.value = g.data?.records || []
  } finally {
    load.value = false
  }
})
</script>
<style scoped lang="scss">
.banner {
  height: 3.6rem;
  width: 100%;
  margin-bottom: 0.32rem;
}
.bn {
  background: linear-gradient(120deg, #7eb6ff, #4b8bfa);
  color: #fff;
  height: 3.2rem;
  line-height: 3.2rem;
  text-align: center;
  font-size: 0.4rem;
}
.navs {
  margin: 0.2rem 0 0.4rem;
  display: flex;
  flex-wrap: wrap;
  gap: 0.16rem;
}
.pname {
  font-size: 0.24rem;
  height: 0.64rem;
  overflow: hidden;
}
.price {
  color: #e74c3c;
  font-size: 0.28rem;
  margin-top: 0.08rem;
}
.tabbar {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  justify-content: space-around;
  background: #fff;
  border-top: 1px solid #eee;
  padding: 0.2rem 0 0.24rem;
  a {
    color: #333;
  }
  a.router-link-active {
    color: $color-primary;
  }
}
</style>
