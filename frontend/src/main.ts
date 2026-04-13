import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'
import elementPlus from './plugins/element-plus'

// 全局样式
import './styles/global.scss'

const app = createApp(App)

app.use(createPinia())
app.use(router)
app.use(elementPlus)

app.mount('#app')