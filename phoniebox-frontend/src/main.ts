import { createApp } from 'vue'
import { createRouter, createWebHistory } from 'vue-router'
import App from './App.vue'
import MediaView from './views/MediaView.vue'
import RadioView from './views/RadioView.vue'
import './style.css'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/',       redirect: '/media' },
    { path: '/media',  component: MediaView, meta: { title: 'Media Library' } },
    { path: '/radio',  component: RadioView, meta: { title: 'Radio Streams' } },
  ],
})

createApp(App).use(router).mount('#app')
