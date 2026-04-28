<template>
  <div v-loading="loading">
    <el-row :gutter="16">
      <el-col :span="6"><el-statistic title="今日曝光" :value="s.todayExpose || 0" /></el-col>
      <el-col :span="6"><el-statistic title="今日点击" :value="s.todayClick || 0" /></el-col>
      <el-col :span="6"><el-statistic title="今日CTR" :value="Number((s.todayCtr || 0) * 100)" suffix="%" :precision="2" /></el-col>
      <el-col :span="6"><el-statistic title="最新任务ID" :value="s.latestTaskId || 0" /></el-col>
    </el-row>
    <el-card style="margin-top: 14px" shadow="never">
      <template #header>
        <div class="hd">
          <span>模型状态</span>
          <div class="ops">
            <el-button :loading="trainLoading" type="primary" @click="doTrain">触发训练</el-button>
            <el-button @click="load">刷新</el-button>
          </div>
        </div>
      </template>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="模型版本">{{ s.modelVersion || '-' }}</el-descriptions-item>
        <el-descriptions-item label="任务状态">{{ s.status || '-' }}</el-descriptions-item>
        <el-descriptions-item label="开始时间">{{ s.createTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="完成时间">{{ s.finishTime || '-' }}</el-descriptions-item>
      </el-descriptions>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { recommendModelStatus, recommendTrain } from '@/api/admin'

const s = reactive({})
const loading = ref(false)
const trainLoading = ref(false)

async function load() {
  loading.value = true
  try {
    const t = await recommendModelStatus()
    Object.assign(s, t.data || {})
  } finally {
    loading.value = false
  }
}

async function doTrain() {
  trainLoading.value = true
  try {
    const t = await recommendTrain()
    ElMessage.success('训练任务已触发：' + (t.data?.taskId || '-'))
    await load()
  } finally {
    trainLoading.value = false
  }
}

onMounted(load)
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
</style>
