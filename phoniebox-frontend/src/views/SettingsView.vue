<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { listSoundCards, selectSoundCard, type SoundCard } from '@/api/settings'

const soundCards = ref<SoundCard[]>([])
const loading    = ref(false)
const saving     = ref(false)
const error      = ref<string | null>(null)

async function load() {
  loading.value = true
  error.value = null
  try {
    soundCards.value = await listSoundCards()
  } catch (e) {
    error.value = String(e)
  } finally {
    loading.value = false
  }
}

async function select(mixerName: string | null) {
  if (saving.value) return
  saving.value = true
  error.value = null
  try {
    soundCards.value = await selectSoundCard(mixerName)
  } catch (e) {
    error.value = String(e)
  } finally {
    saving.value = false
  }
}

const selectedName = () => soundCards.value.find(c => c.selected)?.name ?? null

onMounted(load)
</script>

<template>
  <div>
    <h2 class="text-2xl font-semibold mb-6">Settings</h2>

    <p v-if="error" class="mb-4 p-3 bg-red-900/40 border border-red-700 rounded-lg text-red-300 text-sm">
      {{ error }}
    </p>

    <div v-if="loading" class="text-gray-400 text-sm">Loading…</div>

    <div v-else class="space-y-6">
      <section class="p-5 bg-gray-900 border border-gray-700 rounded-xl">
        <h3 class="text-base font-medium text-gray-200 mb-1">Audio output device</h3>
        <p class="text-xs text-gray-500 mb-4">
          Select which sound card to use for playback. Changes take effect with the next track.
        </p>

        <div class="space-y-2">
          <!-- System default option -->
          <label
            class="flex items-start gap-3 p-3 rounded-lg cursor-pointer transition-colors"
            :class="selectedName() === null
              ? 'bg-indigo-900/40 border border-indigo-600'
              : 'border border-gray-700 hover:bg-gray-800'"
          >
            <input
              type="radio"
              name="soundCard"
              :checked="selectedName() === null"
              :disabled="saving"
              class="mt-0.5 accent-indigo-500"
              @change="select(null)"
            />
            <div>
              <div class="text-sm font-medium text-gray-200">System default</div>
              <div class="text-xs text-gray-500">Let the OS choose the output device</div>
            </div>
          </label>

          <!-- Discovered sound cards -->
          <label
            v-for="card in soundCards"
            :key="card.name"
            class="flex items-start gap-3 p-3 rounded-lg cursor-pointer transition-colors"
            :class="card.selected
              ? 'bg-indigo-900/40 border border-indigo-600'
              : 'border border-gray-700 hover:bg-gray-800'"
          >
            <input
              type="radio"
              name="soundCard"
              :checked="card.selected"
              :disabled="saving"
              class="mt-0.5 accent-indigo-500"
              @change="select(card.name)"
            />
            <div>
              <div class="text-sm font-medium text-gray-200">{{ card.name }}</div>
              <div class="text-xs text-gray-500">{{ card.description }}</div>
            </div>
          </label>

          <p v-if="soundCards.length === 0" class="text-sm text-gray-500 py-2">
            No additional output devices detected.
          </p>
        </div>
      </section>
    </div>
  </div>
</template>
