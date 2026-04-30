const SETTINGS_BASE = '/api/settings'
const SOUND_CARDS_BASE = '/api/audio/sound-cards'
const HTTP_TIMEOUTS_BASE = '/api/http/timeouts'

// ── Generic settings ─────────────────────────────────────────────────────────

export interface Setting {
  key: string
  value: string | null
}

export async function getSetting(key: string): Promise<Setting | null> {
  const res = await fetch(`${SETTINGS_BASE}/${encodeURIComponent(key)}`)
  if (res.status === 404) return null
  if (!res.ok) throw new Error(`Settings API error: ${res.status}`)
  return res.json()
}

export async function setSetting(key: string, value: string | null): Promise<Setting> {
  const res = await fetch(`${SETTINGS_BASE}/${encodeURIComponent(key)}`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ value }),
  })
  if (!res.ok) throw new Error(`Settings API error: ${res.status}`)
  return res.json()
}

// ── Sound card discovery & selection ─────────────────────────────────────────

export interface SoundCard {
  name: string
  description: string
  selected: boolean
}

export async function listSoundCards(): Promise<SoundCard[]> {
  const res = await fetch(SOUND_CARDS_BASE)
  if (!res.ok) throw new Error(`Sound cards API error: ${res.status}`)
  return res.json()
}

export async function selectSoundCard(mixerName: string | null): Promise<SoundCard[]> {
  const res = await fetch(`${SOUND_CARDS_BASE}/selected`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ mixerName }),
  })
  if (!res.ok) throw new Error(`Sound cards API error: ${res.status}`)
  return res.json()
}

// ── HTTP timeouts ─────────────────────────────────────────────────────────────

export interface HttpTimeouts {
  connectTimeoutSeconds: number
  readTimeoutSeconds: number
  writeTimeoutSeconds: number
}

export async function getHttpTimeouts(): Promise<HttpTimeouts> {
  const res = await fetch(HTTP_TIMEOUTS_BASE)
  if (!res.ok) throw new Error(`HTTP timeouts API error: ${res.status}`)
  return res.json()
}

export async function setHttpTimeouts(timeouts: HttpTimeouts): Promise<HttpTimeouts> {
  const res = await fetch(HTTP_TIMEOUTS_BASE, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(timeouts),
  })
  if (!res.ok) throw new Error(`HTTP timeouts API error: ${res.status}`)
  return res.json()
}
