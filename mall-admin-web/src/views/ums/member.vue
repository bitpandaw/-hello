<template>
  <div>
    <el-form inline @submit.prevent="load">
      <el-form-item label="用户名">
        <el-input v-model="q" clearable />
      </el-form-item>
      <el-button type="primary" @click="load">查询</el-button>
    </el-form>
    <el-table v-loading="ld" :data="rows" border>
      <el-table-column prop="id" width="80" label="ID" />
      <el-table-column prop="username" min-width="120" label="用户名" />
      <el-table-column prop="phone" width="120" label="手机" />
      <el-table-column prop="status" width="100" label="状态" />
      <el-table-column width="120" label="操作">
        <template #default="{ row }">
          <el-button type="warning" text @click="toggle(row)">{{
            row.status === 1 ? '封禁' : '解封'
          }}</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination
      v-model:current-page="p"
      v-model:page-size="s"
      :total="total"
      layout="total, prev, pager, next"
      @current-change="load"
    />
  </div>
</template>
<script setup>
import { ref } from 'vue'
import { pageUms, setUmsStatus } from '@/api/admin'
import { ElMessage, ElMessageBox } from 'element-plus'
const q = ref('')
const p = ref(1)
const s = ref(10)
const total = ref(0)
const rows = ref([])
const ld = ref(true)
async function load() {
  ld.value = true
  const t = await pageUms(p.value, s.value, q.value || undefined)
  rows.value = t.data?.records || []
  total.value = t.data?.total || 0
  ld.value = false
}
async function toggle(row) {
  const next = row.status === 1 ? 0 : 1
  try {
    await ElMessageBox.confirm('确认？', '提示', { type: 'warning' })
  } catch {
    return
  }
  await setUmsStatus(row.id, next)
  ElMessage.success('已更新')
  load()
}
load()
</script>
<script>
export default { name: 'UmsMember' }
</script>
