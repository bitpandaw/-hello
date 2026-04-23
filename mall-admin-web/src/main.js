import { createApp } from 'vue'
import { createPinia } from 'pinia'
import piniaPluginPersistedstate from 'pinia-plugin-persistedstate'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import zh from 'element-plus/es/locale/lang/zh-cn.mjs'
import * as ElIcons from '@element-plus/icons-vue'
import App from './App.vue'
import router from './router'
import './styles/main.scss'
import 'nprogress/nprogress.css'

const app = createApp(App)
const pinia = createPinia()
pinia.use(piniaPluginPersistedstate)
app.use(pinia)
app.use(router)
app.use(ElementPlus, { locale: zh })
for (const [k, c] of Object.entries(ElIcons)) {
  app.component(k, c)
}
app.mount('#app')
