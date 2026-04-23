<template>
  <div>
    <el-form inline @submit.prevent="load">
      <el-form-item label="名称">
        <el-input v-model="q" clearable />
      </el-form-item>
      <el-button type="primary" @click="load">查询</el-button>
      <el-button @click="open()">新增</el-button>
    </el-form>
    <el-table v-loading="ld" :data="rows" border stripe :resizable="true" style="width: 100%">
      <el-table-column prop="id" width="80" label="ID" />
      <el-table-column prop="name" min-width="160" label="名称" />
      <el-table-column prop="minPrice" width="100" label="价" />
      <el-table-column prop="publishStatus" width="90" label="上架" />
      <el-table-column label="操作" width="120" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" text @click="open(row)">编辑</el-button>
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
    <el-dialog v-model="show" :title="form.id ? '编辑 SPU' : '新增 SPU'" width="800px" destroy-on-close>
      <el-form label-width="100px">
        <el-form-item label="名称"
          ><el-input v-model="form.name"
        /></el-form-item>
        <el-form-item label="副标题"
          ><el-input v-model="form.subTitle"
        /></el-form-item>
        <el-form-item label="品牌"
          ><el-input-number v-model="form.brandId" :min="0" controls-position="right" class="w"
        /></el-form-item>
        <el-form-item label="分类"
          ><el-input-number v-model="form.categoryId" :min="0" controls-position="right" class="w"
        /></el-form-item>
        <el-form-item label="封面">
          <el-upload :show-file-list="false" :http-request="onCover" accept="image/*">
            <el-image v-if="form.coverImg" :src="abs(form.coverImg)" class="ig" fit="cover" />
            <el-button v-else>上传</el-button>
          </el-upload>
        </el-form-item>
        <el-form-item label="min价"
          ><el-input-number v-model="form.minPrice" :min="0" :step="0.01" :precision="2" class="w"
        /></el-form-item>
        <el-form-item label="售价"
          ><el-input-number v-model="form.originalPrice" :min="0" :step="0.01" :precision="2" class="w"
        /></el-form-item>
        <el-form-item label="状态">
          <el-input-number v-model="form.publishStatus" :min="0" :max="1" controls-position="right" class="w" />
          <el-input-number v-model="form.verifyStatus" :min="0" :max="1" controls-position="right" class="w ml" />
        </el-form-item>
        <el-form-item label="详情(富文本)">
          <div class="ed">
            <Toolbar
              :editor="editorRef"
              :default-config="tbConf"
              mode="default"
              style="border-bottom: 1px solid #ccc"
            />
            <Editor
              v-model="form.detailHtml"
              :default-config="edConf"
              mode="default"
              @onCreated="onCreated"
              style="height: 300px; overflow-y: auto"
            />
          </div>
        </el-form-item>
        <el-form-item
          ><el-button type="primary" :loading="saving" @click="save">保存</el-button></el-form-item
        >
      </el-form>
    </el-dialog>
  </div>
</template>
<script setup>
import '@wangeditor/editor/dist/css/style.css'
import { Editor, Toolbar } from '@wangeditor/editor-for-vue'
import { onBeforeUnmount, ref, shallowRef, reactive } from 'vue'
import { pageProducts, saveProduct, uploadPms } from '@/api/admin'
import { base } from '@/utils/request'
import { ElMessage } from 'element-plus'

const edConf = { placeholder: '富文本，用于前台详情', MENU_CONF: {} }
const tbConf = {}
const editorRef = shallowRef()
const form = reactive({
  id: undefined,
  name: '',
  subTitle: '',
  brandId: 0,
  categoryId: 0,
  coverImg: '',
  minPrice: 0,
  originalPrice: 0,
  detailHtml: '',
  publishStatus: 1,
  verifyStatus: 1,
})
const show = ref(false)
const q = ref('')
const p = ref(1)
const s = ref(10)
const total = ref(0)
const rows = ref([])
const ld = ref(true)
const saving = ref(false)

function abs(u) {
  if (!u) {
    return ''
  }
  if (u.startsWith('http') || u.startsWith('//')) {
    return u
  }
  return (base || '') + u
}

async function load() {
  ld.value = true
  const t = await pageProducts(p.value, s.value, q.value || undefined)
  rows.value = t.data?.records || []
  total.value = t.data?.total || 0
  ld.value = false
}
function open(r) {
  if (r) {
    Object.assign(form, {
      id: r.id,
      name: r.name,
      subTitle: r.subTitle,
      brandId: r.brandId,
      categoryId: r.categoryId,
      coverImg: r.coverImg,
      minPrice: r.minPrice,
      originalPrice: r.originalPrice,
      detailHtml: r.detailHtml || '',
      publishStatus: r.publishStatus,
      verifyStatus: r.verifyStatus,
    })
  } else {
    Object.assign(form, {
      id: undefined,
      name: '',
      subTitle: '',
      brandId: 0,
      categoryId: 0,
      coverImg: '',
      minPrice: 0,
      originalPrice: 0,
      detailHtml: '',
      publishStatus: 1,
      verifyStatus: 1,
    })
  }
  show.value = true
}
async function onCover({ file }) {
  const r = await uploadPms(file)
  form.coverImg = r.data
  ElMessage.success('已上传')
}
async function save() {
  saving.value = true
  try {
    await saveProduct(form)
    ElMessage.success('已保存')
    show.value = false
    load()
  } finally {
    saving.value = false
  }
}
function onCreated(editor) {
  editorRef.value = editor
}
onBeforeUnmount(() => {
  const e = editorRef.value
  if (e && e.destroy) {
    e.destroy()
  }
})
load()
</script>
<script>
export default { name: 'PmsProduct' }
</script>
<style scoped lang="scss">
.w {
  width: 100%;
  max-width: 240px;
}
.ml {
  margin-left: 8px;
}
.ig {
  width: 100px;
  height: 100px;
  border: 1px solid #ebeef5;
  border-radius: 4px;
}
.ed {
  width: 100%;
  max-width: 100%;
  border: 1px solid #ccc;
  z-index: 10;
}
</style>
