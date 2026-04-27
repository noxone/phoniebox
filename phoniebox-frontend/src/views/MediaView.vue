<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { listMediaFiles, uploadMediaFile, updateMediaFileTags, deleteMediaFile, type MediaFile, type UpdateTagsPayload } from '@/api/media'
import { getPlaybackState, playTrack, resumePlayback, pausePlayback, stopPlayback, type PlaybackState } from '@/api/audio'

const MEDIA_FILE_KIND = 'MEDIA_FILE'

const files    = ref<MediaFile[]>([])
const loading  = ref(false)
const error    = ref<string | null>(null)
const uploading = ref(false)

// Edit modal state
const editing  = ref<MediaFile | null>(null)
const editForm = reactive<UpdateTagsPayload>({ trackTitle: null, trackArtist: null, trackAlbum: null, trackGenre: null })
const saving   = ref(false)

// Playback state
const playback = ref<PlaybackState>({ status: 'IDLE', currentTrackKind: null, currentTrackId: null })
const playbackBusy = ref(false)
let pollTimer: ReturnType<typeof setInterval> | null = null

async function load() {
  loading.value = true
  error.value   = null
  try {
    files.value = await listMediaFiles()
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
    // silently ignore poll failures
  }
}

async function nowPlayingToggle() {
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

async function nowPlayingStop() {
  if (playbackBusy.value) return
  playbackBusy.value = true
  try {
    playback.value = await stopPlayback()
  } finally {
    playbackBusy.value = false
  }
}

async function togglePlay(file: MediaFile) {
  if (playbackBusy.value) return
  playbackBusy.value = true
  try {
    if (playback.value.currentTrackKind === MEDIA_FILE_KIND && playback.value.currentTrackId === file.id) {
      // This track is selected — toggle play/pause
      playback.value = playback.value.status === 'PLAYING'
        ? await pausePlayback()
        : await resumePlayback()
    } else {
      // Different track — switch and play
      playback.value = await playTrack(MEDIA_FILE_KIND, file.id)
    }
  } catch (e) {
    error.value = String(e)
  } finally {
    playbackBusy.value = false
  }
}

async function onFileSelected(event: Event) {
  const input = event.target as HTMLInputElement
  if (!input.files?.length) return
  uploading.value = true
  error.value = null
  try {
    const uploaded = await uploadMediaFile(input.files[0])
    files.value.unshift(uploaded)
  } catch (e) {
    error.value = String(e)
  } finally {
    uploading.value = false
    input.value = ''
  }
}

function openEdit(file: MediaFile) {
  editing.value = file
  editForm.trackTitle  = file.trackTitle
  editForm.trackArtist = file.trackArtist
  editForm.trackAlbum  = file.trackAlbum
  editForm.trackGenre  = file.trackGenre
}

function closeEdit() {
  editing.value = null
}

async function saveEdit() {
  if (!editing.value) return
  saving.value = true
  error.value  = null
  try {
    const updated = await updateMediaFileTags(editing.value.id, { ...editForm })
    const idx = files.value.findIndex(f => f.id === updated.id)
    if (idx !== -1) files.value[idx] = updated
    closeEdit()
  } catch (e) {
    error.value = String(e)
  } finally {
    saving.value = false
  }
}

async function remove(id: string) {
  if (!confirm('Delete this file?')) return
  try {
    await deleteMediaFile(id)
    files.value = files.value.filter(f => f.id !== id)
    if (playback.value.currentTrackKind === MEDIA_FILE_KIND && playback.value.currentTrackId === id) {
      playback.value = { status: 'IDLE', currentTrackKind: null, currentTrackId: null }
    }
  } catch (e) {
    error.value = String(e)
  }
}

function formatBytes(bytes: number): string {
  if (bytes < 1024) return `${bytes} B`
  if (bytes < 1024 ** 2) return `${(bytes / 1024).toFixed(1)} KB`
  return `${(bytes / 1024 ** 2).toFixed(1)} MB`
}

function formatDate(iso: string): string {
  return new Date(iso).toLocaleString()
}

function formatDuration(seconds: number | null): string {
  if (seconds == null) return '—'
  const m = Math.floor(seconds / 60)
  const s = seconds % 60
  return `${m}:${String(s).padStart(2, '0')}`
}

function displayAlbum(file: MediaFile): string {
  if (!file.trackAlbum) return '—'
  return file.trackYear ? `${file.trackAlbum} (${file.trackYear})` : file.trackAlbum
}

function nowPlayingLabel(): string {
  if (!playback.value.currentTrackId) return ''
  if (playback.value.currentTrackKind === MEDIA_FILE_KIND) {
    const f = files.value.find(f => f.id === playback.value.currentTrackId)
    return f ? (f.trackTitle ?? f.originalFileName) : playback.value.currentTrackId
  }
  return playback.value.currentTrackId
}

onMounted(async () => {
  await Promise.all([load(), refreshPlayback()])
  pollTimer = setInterval(refreshPlayback, 2000)
})

onUnmounted(() => {
  if (pollTimer !== null) clearInterval(pollTimer)
})
</script>

<template>
  <div>
    <div class="flex items-center justify-between mb-6">
      <h2 class="text-2xl font-semibold">Media Library</h2>

      <label class="cursor-pointer inline-flex items-center gap-2 px-4 py-2
                     bg-indigo-600 hover:bg-indigo-500 rounded-lg text-sm font-medium
                     transition-colors"
             :class="{ 'opacity-60 cursor-not-allowed': uploading }">
        <span>{{ uploading ? 'Uploading…' : 'Upload file' }}</span>
        <input type="file" class="hidden" :disabled="uploading" @change="onFileSelected" />
      </label>
    </div>

    <!-- Now-playing bar -->
    <div
      v-if="playback.status !== 'IDLE'"
      class="flex items-center gap-3 mb-4 px-4 py-3 rounded-lg bg-gray-800 border border-gray-700"
    >
      <span class="text-xs text-gray-400 uppercase tracking-wide shrink-0">
        {{ playback.status === 'PLAYING' ? 'Playing' : 'Paused' }}
      </span>
      <span class="text-sm text-gray-200 font-medium truncate flex-1">{{ nowPlayingLabel() }}</span>
      <button
        class="shrink-0 w-8 h-8 flex items-center justify-center rounded-full
               bg-indigo-600 hover:bg-indigo-500 transition-colors disabled:opacity-50"
        :disabled="playbackBusy"
        @click="nowPlayingToggle"
      >
        <svg v-if="playback.status === 'PLAYING'" xmlns="http://www.w3.org/2000/svg" class="w-4 h-4" viewBox="0 0 24 24" fill="currentColor">
          <rect x="6" y="4" width="4" height="16" rx="1"/><rect x="14" y="4" width="4" height="16" rx="1"/>
        </svg>
        <svg v-else xmlns="http://www.w3.org/2000/svg" class="w-4 h-4" viewBox="0 0 24 24" fill="currentColor">
          <polygon points="5,3 19,12 5,21"/>
        </svg>
      </button>
      <button
        class="shrink-0 w-8 h-8 flex items-center justify-center rounded-full
               bg-gray-700 hover:bg-gray-600 transition-colors disabled:opacity-50"
        :disabled="playbackBusy"
        title="Stop"
        @click="nowPlayingStop"
      >
        <svg xmlns="http://www.w3.org/2000/svg" class="w-4 h-4" viewBox="0 0 24 24" fill="currentColor">
          <rect x="5" y="5" width="14" height="14" rx="1"/>
        </svg>
      </button>
    </div>

    <p v-if="error" class="mb-4 p-3 bg-red-900/40 border border-red-700 rounded-lg text-red-300 text-sm">
      {{ error }}
    </p>

    <div v-if="loading" class="text-gray-400 text-sm">Loading…</div>

    <div v-else-if="files.length === 0" class="text-gray-500 text-sm">
      No files uploaded yet.
    </div>

    <table v-else class="w-full text-sm">
      <thead>
        <tr class="border-b border-gray-800 text-gray-400 text-left">
          <th class="pb-2 pr-2 font-medium w-8"></th>
          <th class="pb-2 pr-4 font-medium">Title</th>
          <th class="pb-2 pr-4 font-medium">Artist</th>
          <th class="pb-2 pr-4 font-medium">Album</th>
          <th class="pb-2 pr-4 font-medium">Genre</th>
          <th class="pb-2 pr-4 font-medium text-right">Duration</th>
          <th class="pb-2 pr-4 font-medium text-right">Size</th>
          <th class="pb-2 pr-4 font-medium">Uploaded</th>
          <th class="pb-2 font-medium"></th>
        </tr>
      </thead>
      <tbody>
        <tr
          v-for="file in files"
          :key="file.id"
          class="border-b border-gray-800/60 hover:bg-gray-800/30 transition-colors"
          :class="{ 'bg-indigo-950/30': playback.currentTrackKind === MEDIA_FILE_KIND && playback.currentTrackId === file.id }"
        >
          <!-- Play / Pause button -->
          <td class="py-3 pr-2">
            <button
              class="w-7 h-7 flex items-center justify-center rounded-full transition-colors disabled:opacity-40"
              :class="playback.currentTrackKind === MEDIA_FILE_KIND && playback.currentTrackId === file.id
                ? 'bg-indigo-600 hover:bg-indigo-500 text-white'
                : 'text-gray-500 hover:text-indigo-400 hover:bg-gray-800'"
              :disabled="playbackBusy"
              :title="playback.currentTrackKind === MEDIA_FILE_KIND && playback.currentTrackId === file.id && playback.status === 'PLAYING' ? 'Pause' : 'Play'"
              @click="togglePlay(file)"
            >
              <!-- Pause icon — shown when this track is actively playing -->
              <svg
                v-if="playback.currentTrackKind === MEDIA_FILE_KIND && playback.currentTrackId === file.id && playback.status === 'PLAYING'"
                xmlns="http://www.w3.org/2000/svg" class="w-3.5 h-3.5" viewBox="0 0 24 24" fill="currentColor"
              >
                <rect x="6" y="4" width="4" height="16" rx="1"/><rect x="14" y="4" width="4" height="16" rx="1"/>
              </svg>
              <!-- Play icon — all other states -->
              <svg
                v-else
                xmlns="http://www.w3.org/2000/svg" class="w-3.5 h-3.5" viewBox="0 0 24 24" fill="currentColor"
              >
                <polygon points="5,3 19,12 5,21"/>
              </svg>
            </button>
          </td>

          <!-- Title: tag title preferred; filename as fallback -->
          <td class="py-3 pr-4 max-w-xs">
            <div class="font-medium text-gray-200 truncate">
              {{ file.trackTitle ?? file.originalFileName }}
            </div>
            <div v-if="file.trackTitle" class="text-xs text-gray-500 truncate font-mono">
              {{ file.originalFileName }}
            </div>
          </td>

          <td class="py-3 pr-4 text-gray-400">{{ file.trackArtist ?? '—' }}</td>

          <td class="py-3 pr-4 text-gray-400">{{ displayAlbum(file) }}</td>

          <td class="py-3 pr-4 text-gray-400">{{ file.trackGenre ?? '—' }}</td>

          <td class="py-3 pr-4 text-gray-400 text-right whitespace-nowrap">
            {{ formatDuration(file.durationSeconds) }}
          </td>

          <td class="py-3 pr-4 text-gray-400 text-right whitespace-nowrap">
            {{ formatBytes(file.sizeInBytes) }}
          </td>

          <td class="py-3 pr-4 text-gray-400 whitespace-nowrap">
            <div>{{ formatDate(file.uploadedAt) }}</div>
            <div v-if="file.updatedAt !== file.uploadedAt" class="text-xs text-gray-500">
              edited {{ formatDate(file.updatedAt) }}
            </div>
          </td>

          <td class="py-3 whitespace-nowrap">
            <button
              class="text-indigo-400 hover:text-indigo-300 transition-colors text-xs mr-3"
              @click="openEdit(file)"
            >
              Edit
            </button>
            <button
              class="text-red-400 hover:text-red-300 transition-colors text-xs"
              @click="remove(file.id)"
            >
              Delete
            </button>
          </td>
        </tr>
      </tbody>
    </table>

    <!-- Edit modal -->
    <Teleport to="body">
      <div
        v-if="editing"
        class="fixed inset-0 z-50 flex items-center justify-center bg-black/60"
        @click.self="closeEdit"
      >
        <div class="bg-gray-900 border border-gray-700 rounded-xl p-6 w-full max-w-md shadow-2xl">
          <h3 class="text-lg font-semibold mb-1">Edit tags</h3>
          <p class="text-xs text-gray-500 mb-5 font-mono truncate">{{ editing.originalFileName }}</p>

          <div class="space-y-4">
            <div>
              <label class="block text-xs text-gray-400 mb-1">Title</label>
              <input
                v-model="editForm.trackTitle"
                type="text"
                placeholder="Track title"
                class="w-full px-3 py-2 rounded-lg bg-gray-800 border border-gray-700 text-sm text-gray-100
                       placeholder-gray-600 focus:outline-none focus:border-indigo-500"
              />
            </div>
            <div>
              <label class="block text-xs text-gray-400 mb-1">Artist</label>
              <input
                v-model="editForm.trackArtist"
                type="text"
                placeholder="Artist name"
                class="w-full px-3 py-2 rounded-lg bg-gray-800 border border-gray-700 text-sm text-gray-100
                       placeholder-gray-600 focus:outline-none focus:border-indigo-500"
              />
            </div>
            <div>
              <label class="block text-xs text-gray-400 mb-1">Album</label>
              <input
                v-model="editForm.trackAlbum"
                type="text"
                placeholder="Album title"
                class="w-full px-3 py-2 rounded-lg bg-gray-800 border border-gray-700 text-sm text-gray-100
                       placeholder-gray-600 focus:outline-none focus:border-indigo-500"
              />
            </div>
            <div>
              <label class="block text-xs text-gray-400 mb-1">Genre</label>
              <input
                v-model="editForm.trackGenre"
                type="text"
                placeholder="Genre"
                class="w-full px-3 py-2 rounded-lg bg-gray-800 border border-gray-700 text-sm text-gray-100
                       placeholder-gray-600 focus:outline-none focus:border-indigo-500"
              />
            </div>
          </div>

          <div class="flex justify-end gap-3 mt-6">
            <button
              class="px-4 py-2 rounded-lg text-sm text-gray-400 hover:text-gray-200 transition-colors"
              :disabled="saving"
              @click="closeEdit"
            >
              Cancel
            </button>
            <button
              class="px-4 py-2 rounded-lg text-sm font-medium bg-indigo-600 hover:bg-indigo-500
                     transition-colors disabled:opacity-60"
              :disabled="saving"
              @click="saveEdit"
            >
              {{ saving ? 'Saving…' : 'Save' }}
            </button>
          </div>
        </div>
      </div>
    </Teleport>
  </div>
</template>
