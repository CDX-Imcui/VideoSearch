import {createApp} from 'vue'
import './style.css'
import App from './App.vue'
import router from './router'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
// 图标引入（必要，否则 prefix-icon 无效）
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import {createPinia} from "pinia";

const app = createApp(App)
app.use(router)//使用路由
app.use(ElementPlus)
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
    app.component(key, component)
}
app.use(createPinia())
app.mount('#app')
