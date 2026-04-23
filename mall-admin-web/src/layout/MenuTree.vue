<template>
  <template v-for="i in items" :key="i.id">
    <el-sub-menu v-if="i.children && i.children.length" :index="subIndex(i)">
      <template #title>{{ i.name }}</template>
      <el-menu-item v-for="c in i.children" :key="c.id" :index="formatPath(c.path)">{{
        c.name
      }}</el-menu-item>
    </el-sub-menu>
    <el-menu-item v-else :index="formatPath(i.path)">{{ i.name }}</el-menu-item>
  </template>
</template>
<script setup>
defineProps({ items: { type: Array, default: () => [] } })
function formatPath(p) {
  if (!p) {
    return '/'
  }
  return p.startsWith('/') ? p : '/' + p
}
function subIndex(i) {
  return String(i.id)
}
</script>
