const BASE = '/api/audio/playback'

export type PlaybackStatus = 'IDLE' | 'PLAYING' | 'PAUSED'

export interface PlaybackState {
  status: PlaybackStatus
  currentTrackKind: string | null
  currentTrackId: string | null
}

async function post(path: string): Promise<PlaybackState> {
  const res = await fetch(`${BASE}${path}`, { method: 'POST' })
  if (!res.ok) throw new Error(`Audio API error: ${res.status}`)
  return res.json()
}

export async function getPlaybackState(): Promise<PlaybackState> {
  const res = await fetch(BASE)
  if (!res.ok) throw new Error(`Audio API error: ${res.status}`)
  return res.json()
}

/** Select a source and start playing immediately. */
export async function playTrack(kind: string, id: string): Promise<PlaybackState> {
  return post(`/play/${kind}/${id}`)
}

/** Resume the currently selected source. */
export async function resumePlayback(): Promise<PlaybackState> {
  return post('/play')
}

/** Pause the currently playing source. */
export async function pausePlayback(): Promise<PlaybackState> {
  return post('/pause')
}

/** Stop playback and clear the current source. */
export async function stopPlayback(): Promise<PlaybackState> {
  return post('/stop')
}

const VOLUME_BASE = '/api/audio/volume'

export async function getVolume(): Promise<number> {
  const res = await fetch(VOLUME_BASE)
  if (!res.ok) throw new Error(`Audio API error: ${res.status}`)
  const data: { volume: number } = await res.json()
  return data.volume
}

export async function setVolume(volume: number): Promise<number> {
  const res = await fetch(VOLUME_BASE, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ volume }),
  })
  if (!res.ok) throw new Error(`Audio API error: ${res.status}`)
  const data: { volume: number } = await res.json()
  return data.volume
}
