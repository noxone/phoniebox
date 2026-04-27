<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { listStreams, addStream, deleteStream, type AudioStream } from '@/api/streams'
import { playTrack, getPlaybackState, pausePlayback, resumePlayback, type PlaybackState } from '@/api/audio'

const AUDIO_STREAM_KIND = 'AUDIO_STREAM'

const streams   = ref<AudioStream[]>([])
const loading   = ref(false)
const error     = ref<string | null>(null)
const adding    = ref(false)
const showForm  = ref(false)
const form      = reactive({ name: '', url: '', mimeType: 'audio/mpeg' })

const playback      = ref<PlaybackState>({ status: 'IDLE', currentTrackKind: null, currentTrackId: null })
const playbackBusy  = ref(false)

async function load() {
  loading.value = true
  error.value = null
  try {
    streams.value = await listStreams()
  } catch (e) {
    error.value = String(e)
  } finally {
    loading.value = false
  }
}

async function refreshPlayback() {
  try {
    playback.value = await getPlaybackState()
  } catch {
    // ignore
  }
}

async function togglePlay(stream: AudioStream) {
  if (playbackBusy.value) return
  playbackBusy.value = true
  try {
    if (playback.value.currentTrackKind === AUDIO_STREAM_KIND && playback.value.currentTrackId === stream.id) {
      playback.value = playback.value.status === 'PLAYING'
        ? await pausePlayback()
        : await resumePlayback()
    } else {
      playback.value = await playTrack(AUDIO_STREAM_KIND, stream.id)
    }
  } catch (e) {
    error.value = String(e)
  } finally {
    playbackBusy.value = false
  }
}

async function submitAdd() {
  if (!form.name || !form.url) return
  adding.value = true
  error.value = null
  try {
    const created = await addStream({ name: form.name, url: form.url, mimeType: form.mimeType })
    streams.value.push(created)
    form.name = ''
    form.url = ''
    form.mimeType = 'audio/mpeg'
    showForm.value = false
  } catch (e) {
    error.value = String(e)
  } finally {
    adding.value = false
  }
}

async function remove(id: string) {
  if (!confirm('Delete this stream?')) return
  try {
    await deleteStream(id)
    streams.value = streams.value.filter(s => s.id !== id)
  } catch (e) {
    error.value = String(e)
  }
}

onMounted(async () => {
  await Promise.all([load(), refreshPlayback()])
})
</script>

<template>
  <div>
    <div class="flex items-center justify-between mb-6">
      <h2 class="text-2xl font-semibold">Radio Streams</h2>
      <button
        class="inline-flex items-center gap-2 px-4 py-2 bg-indigo-600 hover:bg-indigo-500
               rounded-lg text-sm font-medium transition-colors"
        @click="showForm = !showForm"
      >
        {{ showForm ? 'Cancel' : 'Add stream' }}
      </button>
    </div>

    <!-- Add form -->
    <div v-if="showForm" class="mb-6 p-4 bg-gray-900 border border-gray-700 rounded-xl space-y-3">
      <div>
        <label class="block text-xs text-gray-400 mb-1">Name</label>
        <input v-model="form.name" type="text" placeholder="My Radio Station"
               class="w-full px-3 py-2 rounded-lg bg-gray-800 border border-gray-700 text-sm text-gray-100
                      placeholder-gray-600 focus:outline-none focus:border-indigo-500" />
      </div>
      <div>
        <label class="block text-xs text-gray-400 mb-1">Stream URL</label>
        <input v-model="form.url" type="text" placeholder="http://..."
               class="w-full px-3 py-2 rounded-lg bg-gray-800 border border-gray-700 text-sm text-gray-100
                      placeholder-gray-600 focus:outline-none focus:border-indigo-500" />
      </div>
      <div>
        <label class="block text-xs text-gray-400 mb-1">MIME type</label>
        <input v-model="form.mimeType" type="text" placeholder="audio/mpeg"
               class="w-full px-3 py-2 rounded-lg bg-gray-800 border border-gray-700 text-sm text-gray-100
                      placeholder-gray-600 focus:outline-none focus:border-indigo-500" />
      </div>
      <div class="flex justify-end">
        <button
          class="px-4 py-2 rounded-lg text-sm font-medium bg-indigo-600 hover:bg-indigo-500
                 transition-colors disabled:opacity-60"
          :disabled="adding || !form.name || !form.url"
          @click="submitAdd"
        >
          {{ adding ? 'Adding…' : 'Add' }}
        </button>
      </div>
    </div>

    <p v-if="error" class="mb-4 p-3 bg-red-900/40 border border-red-700 rounded-lg text-red-300 text-sm">
      {{ error }}
    </p>

    <div v-if="loading" class="text-gray-400 text-sm">Loading…</div>

    <div v-else-if="streams.length === 0" class="text-gray-500 text-sm">
      No radio streams added yet.
    </div>

    <table v-else class="w-full text-sm">
      <thead>
        <tr class="border-b border-gray-800 text-gray-400 text-left">
          <th class="pb-2 pr-2 font-medium w-8"></th>
          <th class="pb-2 pr-4 font-medium">Name</th>
          <th class="pb-2 pr-4 font-medium">URL</th>
          <th class="pb-2 pr-4 font-medium">MIME type</th>
          <th class="pb-2 font-medium"></th>
        </tr>
      </thead>
      <tbody>
        <tr
          v-for="stream in streams"
          :key="stream.id"
          class="border-b border-gray-800/60 hover:bg-gray-800/30 transition-colors"
          :class="{ 'bg-indigo-950/30': playback.currentTrackKind === AUDIO_STREAM_KIND && playback.currentTrackId === stream.id }"
        >
          <td class="py-3 pr-2">
            <button
              class="w-7 h-7 flex items-center justify-center rounded-full transition-colors disabled:opacity-40"
              :class="playback.currentTrackKind === AUDIO_STREAM_KIND && playback.currentTrackId === stream.id
                ? 'bg-indigo-600 hover:bg-indigo-500 text-white'
                : 'text-gray-500 hover:text-indigo-400 hover:bg-gray-800'"
              :disabled="playbackBusy"
              @click="togglePlay(stream)"
            >
              <svg
                v-if="playback.currentTrackKind === AUDIO_STREAM_KIND && playback.currentTrackId === stream.id && playback.status === 'PLAYING'"
                xmlns="http://www.w3.org/2000/svg" class="w-3.5 h-3.5" viewBox="0 0 24 24" fill="currentColor"
              >
                <rect x="6" y="4" width="4" height="16" rx="1"/><rect x="14" y="4" width="4" height="16" rx="1"/>
              </svg>
              <svg
                v-else
                xmlns="http://www.w3.org/2000/svg" class="w-3.5 h-3.5" viewBox="0 0 24 24" fill="currentColor"
              >
                <polygon points="5,3 19,12 5,21"/>
              </svg>
            </button>
          </td>

          <td class="py-3 pr-4 text-gray-200 font-medium">{{ stream.name }}</td>
          <td class="py-3 pr-4 text-gray-400 font-mono text-xs truncate max-w-xs">{{ stream.url }}</td>
          <td class="py-3 pr-4 text-gray-400">{{ stream.mimeType }}</td>

          <td class="py-3 whitespace-nowrap">
            <button
              class="text-red-400 hover:text-red-300 transition-colors text-xs"
              @click="remove(stream.id)"
            >
              Delete
            </button>
          </td>
        </tr>
      </tbody>
    </table>
  </div>
</template>
