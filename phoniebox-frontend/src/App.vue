<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { useRoute } from 'vue-router'
import { getPlaybackState, resumePlayback, pausePlayback, stopPlayback, getVolume, setVolume, type PlaybackState } from '@/api/audio'

const route = useRoute()

const navItems = [
  { label: 'Media', path: '/media' },
  { label: 'Radio', path: '/radio' },
  { label: 'Settings', path: '/settings' },
]

const playback = ref<PlaybackState>({ status: 'IDLE', currentTrackKind: null, currentTrackId: null })
const playbackBusy = ref(false)
const volume = ref(80)
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

async function onVolumeChange() {
  try {
    volume.value = await setVolume(volume.value)
  } catch {
    // silently ignore
  }
}

onMounted(async () => {
  await refreshPlayback()
  try {
    volume.value = await getVolume()
  } catch {
    // silently ignore
  }
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
      <div class="border-t border-gray-800 p-3 space-y-2">
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

        <!-- Volume control (always visible) -->
        <div class="flex items-center gap-1.5">
          <svg
            class="w-3.5 h-3.5 shrink-0 text-gray-500"
            xmlns="http://www.w3.org/2000/svg"
            viewBox="0 0 24 24"
            fill="currentColor"
          >
            <path v-if="volume === 0" d="M16.5 12A4.5 4.5 0 0 0 14 7.97v2.21l2.45 2.45c.03-.2.05-.41.05-.63zm2.5 0c0 .94-.2 1.82-.54 2.64l1.51 1.51A8.796 8.796 0 0 0 21 12c0-4.28-2.99-7.86-7-8.77v2.06c2.89.86 5 3.54 5 6.71zM4.27 3 3 4.27 7.73 9H3v6h4l5 5v-6.73l4.25 4.25c-.67.52-1.42.93-2.25 1.18v2.06A8.99 8.99 0 0 0 17.73 18l2 2L21 18.73l-9-9L4.27 3zM12 4 9.91 6.09 12 8.18V4z"/>
            <path v-else-if="volume < 50" d="M18.5 12A4.5 4.5 0 0 0 16 7.97v8.05c1.48-.73 2.5-2.25 2.5-4.02zM5 9v6h4l5 5V4L9 9H5z"/>
            <path v-else d="M3 9v6h4l5 5V4L7 9H3zm13.5 3A4.5 4.5 0 0 0 14 7.97v8.05c1.48-.73 2.5-2.25 2.5-4.02zM14 3.23v2.06c2.89.86 5 3.54 5 6.71s-2.11 5.85-5 6.71v2.06c4.01-.91 7-4.49 7-8.77s-2.99-7.86-7-8.77z"/>
          </svg>
          <input
            type="range"
            min="0"
            max="100"
            :value="volume"
            @input="volume = +($event.target as HTMLInputElement).value"
            @change="onVolumeChange"
            class="flex-1 h-1 accent-indigo-500 cursor-pointer"
          />
          <span class="text-xs text-gray-500 w-6 text-right tabular-nums">{{ volume }}</span>
        </div>
      </div>
    </aside>

    <!-- Main content area -->
    <main class="flex-1 overflow-y-auto p-6">
      <router-view />
    </main>
  </div>
</template>
