<template>
  <el-container class="layout-container">
    <el-aside :width="collapsed ? '64px' : '200px'">
      <div class="logo">Mall Admin</div>
      <el-menu
        :default-active="active"
        :collapse="collapsed"
        :router="true"
        background-color="#1d1e1f"
        text-color="#fff"
        active-text-color="#ffd04b"
      >
        <MenuTree :items="items" />
      </el-menu>
    </el-aside>
    <el-container>
      <el-header>
        <el-icon class="fold" @click="collapsed = !collapsed"><Fold v-if="!collapsed" /><Expand v-else /></el-icon>
        <span class="t">{{ $route.meta.title || '管理后台' }}</span>
        <el-button type="danger" text class="out" @click="logout">退出</el-button>
      </el-header>
      <el-main>
        <router-view v-slot="{ Component, route: r }">
          <component :is="Component" :key="r.name || r.path" />
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>
<script setup>
import { computed, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Fold, Expand } from '@element-plus/icons-vue'
import { useAdminStore } from '@/stores/admin'
import MenuTree from './MenuTree.vue'
const collapsed = ref(false)
const route = useRoute()
const router = useRouter()
const a = useAdminStore()
const items = computed(() => a.menuTree || [])
const active = ref(route.path)
watch(
  () => route.path,
  (p) => (active.value = p)
)
function logout() {
  a.clear()
  router.push('/login')
}
</script>
<script>
export default { name: 'LayoutRoot' }
</script>
<style scoped lang="scss">
.layout-container {
  min-height: 100vh;
}
.el-aside {
  background: $bg-aside;
  transition: width 0.2s;
}
.logo {
  color: #fff;
  text-align: center;
  line-height: $header-h;
  font-weight: 600;
}
.el-header {
  display: flex;
  align-items: center;
  height: $header-h;
  border-bottom: 1px solid #ebeef5;
  .t {
    flex: 1;
    margin-left: 12px;
  }
  .out {
    margin-left: auto;
  }
  .fold {
    cursor: pointer;
    font-size: 20px;
  }
}
</style>
