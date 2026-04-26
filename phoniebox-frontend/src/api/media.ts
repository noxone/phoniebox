const BASE = '/api/media'

export interface MediaFile {
  id: string
  originalFileName: string
  mimeType: string
  sizeInBytes: number
  uploadedAt: string   // ISO-8601
  updatedAt: string    // ISO-8601; equals uploadedAt if never edited
  // audio metadata — null when extraction failed or file has no tags
  durationSeconds: number | null
  bitrateKbps: number | null
  sampleRateHz: number | null
  trackTitle: string | null
  trackArtist: string | null
  trackAlbum: string | null
  trackNumber: number | null
  trackYear: number | null
  trackGenre: string | null
}

export interface UpdateTagsPayload {
  trackTitle: string | null
  trackArtist: string | null
  trackAlbum: string | null
  trackGenre: string | null
}

export async function listMediaFiles(): Promise<MediaFile[]> {
  const res = await fetch(BASE)
  if (!res.ok) throw new Error(`Failed to list media files: ${res.status}`)
  return res.json()
}

export async function getMediaFile(id: string): Promise<MediaFile> {
  const res = await fetch(`${BASE}/${id}`)
  if (!res.ok) throw new Error(`Media file not found: ${id}`)
  return res.json()
}

export async function uploadMediaFile(file: File): Promise<MediaFile> {
  const form = new FormData()
  form.append('file', file)
  const res = await fetch(BASE, { method: 'POST', body: form })
  if (!res.ok) throw new Error(`Upload failed: ${res.status}`)
  return res.json()
}

export async function updateMediaFileTags(id: string, payload: UpdateTagsPayload): Promise<MediaFile> {
  const res = await fetch(`${BASE}/${id}`, {
    method: 'PATCH',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload),
  })
  if (!res.ok) throw new Error(`Update failed: ${res.status}`)
  return res.json()
}

export async function deleteMediaFile(id: string): Promise<void> {
  const res = await fetch(`${BASE}/${id}`, { method: 'DELETE' })
  if (!res.ok) throw new Error(`Delete failed: ${res.status}`)
}
