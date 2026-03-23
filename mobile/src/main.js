import { createSSRApp } from 'vue'
import App from './App.vue'
import { createPinia } from 'pinia'
import uviewUI from 'uview-ui'

export function createApp() {
  const app = createSSRApp(App)
  const pinia = createPinia()
  
  app.use(pinia)
  app.use(uviewUI)
  
  return {
    app,
    pinia
  }
}
