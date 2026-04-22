<script setup lang="ts">
import { useRouter, useRoute } from 'vue-router'

const router = useRouter()
const route  = useRoute()

const navItems = [
  { label: 'Media',    path: '/media' },
  // Future: { label: 'Playlists', path: '/playlists' },
  // Future: { label: 'Radio',     path: '/radio'     },
  // Future: { label: 'Settings',  path: '/settings'  },
]
</script>

<template>
  <div class="flex h-screen overflow-hidden">
    <!-- Sidebar -->
    <aside class="w-56 shrink-0 bg-gray-900 border-r border-gray-800 flex flex-col">
      <div class="p-5 border-b border-gray-800">
        <h1 class="text-xl font-bold tracking-tight text-white">🎵 Phoniebox</h1>
      </div>
      <nav class="flex-1 p-3 space-y-1">
        <router-link
          v-for="item in navItems"
          :key="item.path"
          :to="item.path"
          class="flex items-center px-3 py-2 rounded-lg text-sm transition-colors"
          :class="route.path === item.path
            ? 'bg-indigo-600 text-white'
            : 'text-gray-400 hover:bg-gray-800 hover:text-white'"
        >
          {{ item.label }}
        </router-link>
      </nav>
    </aside>

    <!-- Main content area -->
    <main class="flex-1 overflow-y-auto p-6">
      <router-view />
    </main>
  </div>
</template>
