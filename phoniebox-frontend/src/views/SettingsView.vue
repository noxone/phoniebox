<script setup lang="ts">
import { ref, onMounted } from 'vue'
import {
  listSoundCards,
  selectSoundCard,
  getHttpTimeouts,
  setHttpTimeouts,
  type SoundCard,
  type HttpTimeouts,
} from '@/api/settings'
import { getMaxVolume, setMaxVolume } from '@/api/audio'

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

// ── HTTP timeouts ─────────────────────────────────────────────────────────────

const timeouts     = ref<HttpTimeouts>({ connectTimeoutSeconds: 10, readTimeoutSeconds: 30, writeTimeoutSeconds: 10 })
const timeoutsLoading = ref(false)
const timeoutsSaving  = ref(false)
const timeoutsError   = ref<string | null>(null)

async function loadTimeouts() {
  timeoutsLoading.value = true
  timeoutsError.value = null
  try {
    timeouts.value = await getHttpTimeouts()
  } catch (e) {
    timeoutsError.value = String(e)
  } finally {
    timeoutsLoading.value = false
  }
}

async function saveTimeouts() {
  if (timeoutsSaving.value) return
  timeoutsSaving.value = true
  timeoutsError.value = null
  try {
    timeouts.value = await setHttpTimeouts(timeouts.value)
  } catch (e) {
    timeoutsError.value = String(e)
  } finally {
    timeoutsSaving.value = false
  }
}

// ── Max volume ────────────────────────────────────────────────────────────────

const maxVolume       = ref(100)
const maxVolumeSaving = ref(false)
const maxVolumeError  = ref<string | null>(null)

async function loadMaxVolume() {
  try {
    maxVolume.value = await getMaxVolume()
  } catch (e) {
    maxVolumeError.value = String(e)
  }
}

async function saveMaxVolume() {
  if (maxVolumeSaving.value) return
  maxVolumeSaving.value = true
  maxVolumeError.value = null
  try {
    maxVolume.value = await setMaxVolume(maxVolume.value)
  } catch (e) {
    maxVolumeError.value = String(e)
  } finally {
    maxVolumeSaving.value = false
  }
}

onMounted(() => {
  load()
  loadTimeouts()
  loadMaxVolume()
})
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

      <!-- Max volume -->
      <section class="p-5 bg-gray-900 border border-gray-700 rounded-xl">
        <h3 class="text-base font-medium text-gray-200 mb-1">Maximum volume</h3>
        <p class="text-xs text-gray-500 mb-4">
          Caps the volume slider so it cannot exceed this level. Useful for protecting speakers or
          limiting loudness for children. Changes take effect immediately.
        </p>

        <p v-if="maxVolumeError" class="mb-3 p-3 bg-red-900/40 border border-red-700 rounded-lg text-red-300 text-sm">
          {{ maxVolumeError }}
        </p>

        <div class="flex items-center gap-3">
          <input
            type="range"
            min="0"
            max="100"
            v-model.number="maxVolume"
            :disabled="maxVolumeSaving"
            class="flex-1 h-1 accent-indigo-500 cursor-pointer disabled:opacity-50"
          />
          <span class="text-sm text-gray-300 tabular-nums w-8 text-right">{{ maxVolume }}</span>
          <button
            :disabled="maxVolumeSaving"
            class="px-4 py-2 bg-indigo-600 hover:bg-indigo-500 disabled:opacity-50 text-white text-sm font-medium rounded-lg transition-colors"
            @click="saveMaxVolume"
          >
            {{ maxVolumeSaving ? 'Saving…' : 'Save' }}
          </button>
        </div>
      </section>

      <!-- Network timeouts -->
      <section class="p-5 bg-gray-900 border border-gray-700 rounded-xl">
        <h3 class="text-base font-medium text-gray-200 mb-1">Network timeouts</h3>
        <p class="text-xs text-gray-500 mb-4">
          Timeout values (in seconds) for outgoing HTTP connections used to fetch audio streams.
          Changes take effect with the next connection.
        </p>

        <p v-if="timeoutsError" class="mb-3 p-3 bg-red-900/40 border border-red-700 rounded-lg text-red-300 text-sm">
          {{ timeoutsError }}
        </p>

        <div v-if="timeoutsLoading" class="text-gray-400 text-sm">Loading…</div>

        <div v-else class="space-y-4">
          <div class="grid grid-cols-3 gap-4">
            <div>
              <label class="block text-xs text-gray-400 mb-1">Connect timeout (s)</label>
              <input
                v-model.number="timeouts.connectTimeoutSeconds"
                type="number"
                min="1"
                max="300"
                :disabled="timeoutsSaving"
                class="w-full bg-gray-800 border border-gray-600 rounded-lg px-3 py-2 text-sm text-gray-200 focus:outline-none focus:border-indigo-500 disabled:opacity-50"
              />
            </div>
            <div>
              <label class="block text-xs text-gray-400 mb-1">Read timeout (s)</label>
              <input
                v-model.number="timeouts.readTimeoutSeconds"
                type="number"
                min="1"
                max="300"
                :disabled="timeoutsSaving"
                class="w-full bg-gray-800 border border-gray-600 rounded-lg px-3 py-2 text-sm text-gray-200 focus:outline-none focus:border-indigo-500 disabled:opacity-50"
              />
            </div>
            <div>
              <label class="block text-xs text-gray-400 mb-1">Write timeout (s)</label>
              <input
                v-model.number="timeouts.writeTimeoutSeconds"
                type="number"
                min="1"
                max="300"
                :disabled="timeoutsSaving"
                class="w-full bg-gray-800 border border-gray-600 rounded-lg px-3 py-2 text-sm text-gray-200 focus:outline-none focus:border-indigo-500 disabled:opacity-50"
              />
            </div>
          </div>

          <button
            :disabled="timeoutsSaving"
            class="px-4 py-2 bg-indigo-600 hover:bg-indigo-500 disabled:opacity-50 text-white text-sm font-medium rounded-lg transition-colors"
            @click="saveTimeouts"
          >
            {{ timeoutsSaving ? 'Saving…' : 'Save timeouts' }}
          </button>
        </div>
      </section>
    </div>
  </div>
</template>
