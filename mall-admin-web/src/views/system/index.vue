<template>
  <el-row :gutter="16">
    <el-col :span="8">
      <el-card shadow="never" header="角色列表">
        <el-table
          v-loading="loading.roles"
          :data="roles"
          border
          highlight-current-row
          style="width: 100%"
          @current-change="onRoleChange"
        >
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column prop="name" label="角色名" min-width="120" />
          <el-table-column prop="code" label="编码" min-width="120" />
        </el-table>
      </el-card>
    </el-col>
    <el-col :span="16">
      <el-card shadow="never">
        <template #header>
          <div class="hd">
            <span>权限配置</span>
            <div class="ops">
              <el-button size="small" @click="reloadRolePerms" :disabled="!currentRoleId">刷新授权</el-button>
              <el-button type="primary" size="small" :loading="loading.save" @click="save" :disabled="!currentRoleId">
                保存配置
              </el-button>
            </div>
          </div>
        </template>
        <el-alert
          v-if="!currentRoleId"
          title="请先在左侧选择一个角色"
          type="info"
          show-icon
          :closable="false"
          style="margin-bottom: 12px"
        />
        <el-tree
          ref="treeRef"
          v-loading="loading.tree || loading.perms"
          :data="permTree"
          node-key="id"
          show-checkbox
          check-strictly
          default-expand-all
          :props="{ label: 'name', children: 'children' }"
          empty-text="暂无权限数据"
        >
          <template #default="{ data }">
            <span>
              {{ data.name }}
              <el-tag size="small" type="info" effect="plain" class="tag">{{ data.code || '-' }}</el-tag>
            </span>
          </template>
        </el-tree>
      </el-card>
    </el-col>
  </el-row>
</template>

<script setup>
import { nextTick, onMounted, ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { listRoles, permissionTree, rolePermissionIds, saveRolePermissions } from '@/api/admin'

const roles = ref([])
const permTree = ref([])
const currentRoleId = ref()
const treeRef = ref()
const loading = reactive({ roles: false, tree: false, perms: false, save: false })

async function loadBase() {
  loading.roles = true
  loading.tree = true
  try {
    const [rRes, pRes] = await Promise.all([listRoles(), permissionTree()])
    roles.value = rRes.data || []
    permTree.value = pRes.data || []
  } finally {
    loading.roles = false
    loading.tree = false
  }
}

async function onRoleChange(row) {
  currentRoleId.value = row?.id
  await reloadRolePerms()
}

async function reloadRolePerms() {
  if (!currentRoleId.value || !treeRef.value) {
    return
  }
  loading.perms = true
  try {
    const res = await rolePermissionIds(currentRoleId.value)
    const ids = res.data || []
    await nextTick()
    treeRef.value.setCheckedKeys(ids)
  } finally {
    loading.perms = false
  }
}

async function save() {
  if (!currentRoleId.value || !treeRef.value) {
    ElMessage.warning('请先选择角色')
    return
  }
  loading.save = true
  try {
    const full = treeRef.value.getCheckedKeys(false) || []
    const half = treeRef.value.getHalfCheckedKeys() || []
    const ids = Array.from(new Set([...full, ...half]))
    await saveRolePermissions(currentRoleId.value, ids)
    ElMessage.success('权限已保存')
  } finally {
    loading.save = false
  }
}

onMounted(loadBase)
</script>

<script>
export default { name: 'SystemRolePermission' }
</script>

<style scoped>
.hd {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.ops {
  display: flex;
  gap: 8px;
}
.tag {
  margin-left: 8px;
}
</style>
