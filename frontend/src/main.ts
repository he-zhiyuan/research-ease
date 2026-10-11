import { createApp } from 'vue'
import { createPinia } from 'pinia'
import './style.css'
import './styles/elder-mode.css'
import App from './App.vue'
import router from './router'

if (localStorage.getItem('researchease-elder-mode') === '1') {
  document.documentElement.classList.add('elder-mode')
}

const app = createApp(App)
app.use(createPinia())
app.use(router)
app.mount('#app')
