<template>
  <el-config-provider namespace="el" :message="{ max: 1 }">
    <div class="portal-shell ignore-vw">
      <header class="topbar">
        <div class="inner top-row">
          <div class="brand" @click="goHome">
            <div class="brand-mark">DSP</div>
            <div class="brand-copy">
              <div class="brand-kicker">Spring Boot Graduation Design</div>
              <div class="brand-name">数码销售平台</div>
              <div class="brand-sub">Digital Sales Platform</div>
            </div>
          </div>

          <div class="search-box">
            <el-input
              v-model="keyword"
              placeholder="搜索手机、笔记本、平板、耳机等设备"
              clearable
              @keyup.enter="goSearch"
            >
              <template #append>
                <el-button type="primary" @click="goSearch">进入商品库</el-button>
              </template>
            </el-input>
            <div class="search-hints">
              <span>手机</span>
              <span>笔记本</span>
              <span>平板</span>
              <span>音频设备</span>
            </div>
          </div>

          <div class="user-actions">
            <template v-if="userStore.isLogin">
              <el-button text @click="$router.push('/member')">我的账户</el-button>
              <el-button text @click="$router.push('/order/list')">订单中心</el-button>
              <el-button text @click="logout">退出登录</el-button>
            </template>
            <template v-else>
              <el-button text @click="$router.push('/auth/login')">登录</el-button>
              <el-button text @click="$router.push('/auth/register')">注册</el-button>
            </template>
          </div>
        </div>

        <div class="inner nav-row">
          <nav class="main-nav">
            <router-link to="/home">首页</router-link>
            <router-link to="/category">数码分类</router-link>
            <router-link to="/search">精选设备</router-link>
            <router-link to="/cart">购物车</router-link>
            <router-link to="/member">我的账户</router-link>
          </nav>
          <div class="platform-pills">
            <span>前后端分离演示</span>
            <span>商品与订单流程</span>
            <span>真实设备图展示</span>
          </div>
        </div>
      </header>

      <main class="main">
        <router-view />
      </main>

      <footer class="footer-nav">
        <div class="inner">
          <router-link to="/home">首页</router-link>
          <router-link to="/category">分类</router-link>
          <router-link to="/search">设备</router-link>
          <router-link to="/cart">购物车</router-link>
          <router-link to="/member">账户</router-link>
        </div>
      </footer>
    </div>
  </el-config-provider>
</template>

<script setup>
import { ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const keyword = ref(typeof route.query.q === 'string' ? route.query.q : '')

watch(
  () => route.query.q,
  (q) => {
    keyword.value = typeof q === 'string' ? q : ''
  }
)

function goHome() {
  router.push('/home')
}

function goSearch() {
  const q = keyword.value?.trim()
  router.push(q ? `/search?q=${encodeURIComponent(q)}` : '/search')
}

function logout() {
  userStore.clear()
  router.push('/home')
}
</script>

<style scoped lang="scss">
.ignore-vw.portal-shell {
  min-height: 100%;
}

.ignore-vw .inner {
  width: min($page-max-width, calc(100% - 32px));
  margin: 0 auto;
}

.ignore-vw .topbar {
  position: sticky;
  top: 0;
  z-index: 40;
  backdrop-filter: blur(20px);
  background:
    linear-gradient(180deg, rgba(6, 16, 31, 0.92), rgba(10, 22, 40, 0.84)),
    rgba(10, 22, 40, 0.88);
  border-bottom: 1px solid rgba(83, 123, 178, 0.22);
  box-shadow: 0 16px 40px rgba(7, 15, 31, 0.18);
}

.ignore-vw .top-row {
  display: grid;
  grid-template-columns: minmax(260px, 320px) minmax(440px, 1fr) fit-content(440px);
  gap: 18px;
  align-items: center;
  justify-content: space-between;
  min-height: 80px;
}

.ignore-vw .brand {
  display: flex;
  align-items: center;
  gap: 14px;
  cursor: pointer;
}

.ignore-vw .brand-mark {
  width: 58px;
  height: 58px;
  border-radius: 18px;
  display: grid;
  place-items: center;
  background:
    radial-gradient(circle at 30% 30%, rgba(255, 255, 255, 0.36), transparent 38%),
    linear-gradient(135deg, #1d7bff, #28d7ff 65%, #7df1ff);
  color: #fff;
  font-size: 18px;
  font-weight: 800;
  letter-spacing: 0.06em;
  box-shadow: 0 18px 36px rgba(16, 89, 214, 0.36);
}

.ignore-vw .brand-kicker {
  color: rgba(162, 200, 255, 0.9);
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.16em;
  text-transform: uppercase;
}

.ignore-vw .brand-name {
  margin-top: 4px;
  color: #f5f9ff;
  font-size: 26px;
  font-weight: 800;
  line-height: 1.1;
  letter-spacing: 0.02em;
}

.ignore-vw .brand-sub {
  margin-top: 4px;
  color: rgba(183, 205, 230, 0.82);
  font-size: 12px;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.ignore-vw .search-box {
  display: grid;
  gap: 10px;
  width: min(100%, 760px);
  justify-self: stretch;
}

.ignore-vw .search-box :deep(.el-input__wrapper) {
  min-height: 52px;
  border-radius: 16px 0 0 16px;
  background: rgba(245, 250, 255, 0.96);
  box-shadow: inset 0 0 0 1px rgba(119, 159, 214, 0.14);
}

.ignore-vw .search-box :deep(.el-input-group__append) {
  border-radius: 0 16px 16px 0;
}

.ignore-vw .search-box :deep(.el-input-group__append .el-button) {
  min-height: 52px;
  padding-inline: 20px;
  background: linear-gradient(135deg, #1d7bff, #25c4ff);
  border: none;
  font-weight: 700;
}

.ignore-vw .search-hints {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.ignore-vw .search-hints span {
  padding: 5px 10px;
  border-radius: 999px;
  border: 1px solid rgba(107, 158, 224, 0.24);
  background: rgba(255, 255, 255, 0.06);
  color: rgba(216, 230, 247, 0.92);
  font-size: 12px;
}

.ignore-vw .user-actions {
  display: flex;
  align-items: center;
  gap: 2px;
  justify-self: end;
  white-space: nowrap;
  flex-shrink: 0;
  padding: 4px 6px;
  border-radius: 999px;
  border: 1px solid rgba(107, 158, 224, 0.16);
  background: rgba(255, 255, 255, 0.05);
}

.ignore-vw .user-actions :deep(.el-button) {
  color: #eef5ff;
  font-weight: 600;
  font-size: 15px;
  padding-inline: 10px;
}

.ignore-vw .nav-row {
  min-height: 54px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18px;
  border-top: 1px solid rgba(83, 123, 178, 0.18);
}

.ignore-vw .main-nav {
  display: flex;
  align-items: center;
  gap: 26px;
}

.ignore-vw .main-nav a {
  color: rgba(205, 222, 242, 0.82);
  font-size: 14px;
  font-weight: 700;
  transition: color 0.2s ease, transform 0.2s ease;
}

.ignore-vw .main-nav a:hover {
  color: #fff;
  transform: translateY(-1px);
}

.ignore-vw .main-nav a.router-link-active {
  color: #6fe3ff;
}

.ignore-vw .platform-pills {
  display: none;
}

.ignore-vw .main {
  padding: 10px 0 72px;
}

.ignore-vw .footer-nav {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(255, 255, 255, 0.95);
  border-top: 1px solid rgba(217, 227, 238, 0.92);
  backdrop-filter: blur(16px);
  display: none;
  z-index: 50;
}

.ignore-vw .footer-nav .inner {
  height: 58px;
  display: flex;
  justify-content: space-around;
  align-items: center;
}

.ignore-vw .footer-nav a {
  color: $text-sub;
  font-size: 13px;
}

.ignore-vw .footer-nav a.router-link-active {
  color: $brand-primary;
  font-weight: 700;
}

@media (max-width: 980px) {
  .ignore-vw .top-row {
    grid-template-columns: 1fr;
    gap: 12px;
    padding: 14px 0;
  }

  .ignore-vw .user-actions,
  .ignore-vw .platform-pills {
    display: none;
  }
}

@media (max-width: 860px) {
  .ignore-vw .inner {
    width: calc(100% - 16px);
  }

  .ignore-vw .brand-name {
    font-size: 20px;
  }

  .ignore-vw .brand-kicker,
  .ignore-vw .brand-sub,
  .ignore-vw .search-hints {
    display: none;
  }

  .ignore-vw .brand-mark {
    width: 44px;
    height: 44px;
    border-radius: 14px;
    font-size: 14px;
  }

  .ignore-vw .main-nav {
    display: none;
  }

  .ignore-vw .footer-nav {
    display: block;
  }

  .ignore-vw .main {
    padding-bottom: 70px;
  }
}
</style>
