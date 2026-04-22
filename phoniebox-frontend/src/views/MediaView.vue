<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { listMediaFiles, uploadMediaFile, deleteMediaFile, type MediaFile } from '@/api/media'

const files   = ref<MediaFile[]>([])
const loading = ref(false)
const error   = ref<string | null>(null)
const uploading = ref(false)

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

async function remove(id: string) {
  if (!confirm('Delete this file?')) return
  try {
    await deleteMediaFile(id)
    files.value = files.value.filter(f => f.id !== id)
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

onMounted(load)
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
          <th class="pb-2 pr-4 font-medium">File name</th>
          <th class="pb-2 pr-4 font-medium">Type</th>
          <th class="pb-2 pr-4 font-medium">Size</th>
          <th class="pb-2 pr-4 font-medium">Uploaded</th>
          <th class="pb-2 font-medium"></th>
        </tr>
      </thead>
      <tbody>
        <tr
          v-for="file in files"
          :key="file.id"
          class="border-b border-gray-800/60 hover:bg-gray-800/30 transition-colors"
        >
          <td class="py-3 pr-4 font-mono text-gray-200 truncate max-w-xs">
            {{ file.originalFileName }}
          </td>
          <td class="py-3 pr-4 text-gray-400">{{ file.mimeType }}</td>
          <td class="py-3 pr-4 text-gray-400 whitespace-nowrap">{{ formatBytes(file.sizeInBytes) }}</td>
          <td class="py-3 pr-4 text-gray-400 whitespace-nowrap">{{ formatDate(file.uploadedAt) }}</td>
          <td class="py-3">
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
  </div>
</template>
