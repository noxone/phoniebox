const BASE = '/api/audio/playback'

export type PlaybackStatus = 'IDLE' | 'PLAYING' | 'PAUSED'

export interface PlaybackState {
  status: PlaybackStatus
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

/** Select a track and start playing immediately. */
export async function playTrack(id: string): Promise<PlaybackState> {
  return post(`/play/${id}`)
}

/** Resume the currently selected track. */
export async function resumePlayback(): Promise<PlaybackState> {
  return post('/play')
}

/** Pause the currently playing track. */
export async function pausePlayback(): Promise<PlaybackState> {
  return post('/pause')
}
