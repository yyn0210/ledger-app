import { createApp } from 'vue'
<<<<<<< HEAD
import { createPinia } from 'pinia'
import naive from 'naive-ui'
import App from './App.vue'
import router from './router'

import './assets/styles/variables.css'
import './assets/styles/global.css'

const app = createApp(App)
const pinia = createPinia()

app.use(pinia)
app.use(router)
app.use(naive)

app.mount('#app')
=======
import './style.css'
import App from './App.vue'

createApp(App).mount('#app')
>>>>>>> 1d48db51b0bcbb5434e8d88420eea15f9c38acc3
