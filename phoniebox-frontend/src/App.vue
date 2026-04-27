<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { useRoute } from 'vue-router'
import { getPlaybackState, resumePlayback, pausePlayback, stopPlayback, type PlaybackState } from '@/api/audio'

const route = useRoute()

const navItems = [
  { label: 'Media', path: '/media' },
  { label: 'Radio', path: '/radio' },
]

const playback = ref<PlaybackState>({ status: 'IDLE', currentTrackKind: null, currentTrackId: null })
const playbackBusy = ref(false)
let pollTimer: ReturnType<typeof setInterval> | null = null

async function refreshPlayback() {
  try {
    playback.value = await getPlaybackState()
  } catch {
    // silently ignore poll failures
  }
}

async function togglePlayPause() {
  if (playbackBusy.value) return
  playbackBusy.value = true
  try {
    playback.value = playback.value.status === 'PLAYING'
      ? await pausePlayback()
      : await resumePlayback()
  } finally {
    playbackBusy.value = false
  }
}

async function stop() {
  if (playbackBusy.value) return
  playbackBusy.value = true
  try {
    playback.value = await stopPlayback()
  } finally {
    playbackBusy.value = false
  }
}

onMounted(async () => {
  await refreshPlayback()
  pollTimer = setInterval(refreshPlayback, 2000)
})

onUnmounted(() => {
  if (pollTimer !== null) clearInterval(pollTimer)
})
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

      <!-- Sidebar player bar -->
      <div class="border-t border-gray-800 p-3">
        <div v-if="playback.status === 'IDLE'" class="text-xs text-gray-600 text-center py-1">
          Nothing playing
        </div>
        <div v-else>
          <div class="text-xs text-gray-400 truncate mb-2">
            <span class="uppercase tracking-wide text-gray-600 mr-1">
              {{ playback.status === 'PLAYING' ? 'Playing' : 'Paused' }}
            </span>
            <span class="text-gray-300">{{ playback.currentTrackKind?.replace('_', ' ') }}</span>
          </div>
          <div class="flex items-center gap-2 justify-center">
            <!-- Play / Pause -->
            <button
              class="w-9 h-9 flex items-center justify-center rounded-full bg-indigo-600 hover:bg-indigo-500
                     transition-colors disabled:opacity-50"
              :disabled="playbackBusy"
              @click="togglePlayPause"
            >
              <svg v-if="playback.status === 'PLAYING'" xmlns="http://www.w3.org/2000/svg" class="w-4 h-4" viewBox="0 0 24 24" fill="currentColor">
                <rect x="6" y="4" width="4" height="16" rx="1"/><rect x="14" y="4" width="4" height="16" rx="1"/>
              </svg>
              <svg v-else xmlns="http://www.w3.org/2000/svg" class="w-4 h-4" viewBox="0 0 24 24" fill="currentColor">
                <polygon points="5,3 19,12 5,21"/>
              </svg>
            </button>
            <!-- Stop -->
            <button
              class="w-9 h-9 flex items-center justify-center rounded-full bg-gray-700 hover:bg-gray-600
                     transition-colors disabled:opacity-50"
              :disabled="playbackBusy"
              title="Stop"
              @click="stop"
            >
              <svg xmlns="http://www.w3.org/2000/svg" class="w-4 h-4" viewBox="0 0 24 24" fill="currentColor">
                <rect x="5" y="5" width="14" height="14" rx="1"/>
              </svg>
            </button>
          </div>
        </div>
      </div>
    </aside>

    <!-- Main content area -->
    <main class="flex-1 overflow-y-auto p-6">
      <router-view />
    </main>
  </div>
</template>
