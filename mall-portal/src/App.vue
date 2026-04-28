<template>
  <el-config-provider namespace="el" :message="{ max: 1 }">
    <div class="portal-shell ignore-vw">
      <header class="topbar">
        <div class="inner">
          <div class="logo" @click="goHome">MALL</div>
          <div class="search-box">
            <el-input
              v-model="keyword"
              placeholder="搜索商品、品牌"
              clearable
              @keyup.enter="goSearch"
            >
              <template #append>
                <el-button type="primary" @click="goSearch">搜索</el-button>
              </template>
            </el-input>
          </div>
          <div class="user-actions">
            <template v-if="userStore.isLogin">
              <el-button text @click="$router.push('/member')">我的账号</el-button>
              <el-button text @click="$router.push('/order/list')">我的订单</el-button>
              <el-button text @click="logout">退出</el-button>
            </template>
            <template v-else>
              <el-button text @click="$router.push('/auth/login')">登录</el-button>
              <el-button text @click="$router.push('/auth/register')">注册</el-button>
            </template>
          </div>
        </div>
        <nav class="main-nav inner">
          <router-link to="/home">首页</router-link>
          <router-link to="/category">分类</router-link>
          <router-link to="/search">发现好物</router-link>
          <router-link to="/cart">购物车</router-link>
          <router-link to="/member">个人中心</router-link>
        </nav>
      </header>

      <main class="main">
        <router-view />
      </main>

      <footer class="footer-nav">
        <div class="inner">
          <router-link to="/home">首页</router-link>
          <router-link to="/category">分类</router-link>
          <router-link to="/cart">购物车</router-link>
          <router-link to="/member">我的</router-link>
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
  width: min(1200px, calc(100% - 32px));
  margin: 0 auto;
}

.ignore-vw .topbar {
  position: sticky;
  top: 0;
  z-index: 30;
  background: #fff;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);

  .inner {
    display: flex;
    align-items: center;
    gap: 16px;
    height: 72px;
  }
}

.ignore-vw .logo {
  font-size: 30px;
  line-height: 1;
  font-weight: 800;
  letter-spacing: 1px;
  color: #ff6a00;
  cursor: pointer;
  user-select: none;
}

.ignore-vw .search-box {
  flex: 1;
}

.ignore-vw .user-actions {
  display: flex;
  align-items: center;
  gap: 2px;
}

.ignore-vw .main-nav {
  height: 48px !important;
  display: flex;
  align-items: center;
  gap: 26px;
  border-top: 1px solid #f2f2f2;

  a {
    color: #606266;
    font-size: 14px;
  }

  a.router-link-active {
    color: #ff6a00;
    font-weight: 600;
  }
}

.ignore-vw .main {
  padding: 18px 0 72px;
}

.ignore-vw .footer-nav {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  background: #fff;
  border-top: 1px solid #ececec;
  display: none;

  .inner {
    height: 54px;
    display: flex;
    justify-content: space-around;
    align-items: center;
  }

  a {
    color: #666;
  }

  a.router-link-active {
    color: #ff6a00;
    font-weight: 600;
  }
}

@media (max-width: 860px) {
  .ignore-vw .topbar {
    .inner {
      height: 62px;
      gap: 10px;
    }
  }

  .ignore-vw .logo {
    font-size: 22px;
  }

  .ignore-vw .main-nav {
    display: none;
  }

  .ignore-vw .user-actions {
    display: none;
  }

  .ignore-vw .footer-nav {
    display: block;
  }

  .ignore-vw .main {
    padding-bottom: 66px;
  }
}
</style>
