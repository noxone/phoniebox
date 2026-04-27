const BASE = '/api/streams'

export interface AudioStream {
  id: string
  name: string
  url: string
  mimeType: string
}

export interface AddStreamPayload {
  name: string
  url: string
  mimeType: string
}

export async function listStreams(): Promise<AudioStream[]> {
  const res = await fetch(BASE)
  if (!res.ok) throw new Error(`Streams API error: ${res.status}`)
  return res.json()
}

export async function addStream(payload: AddStreamPayload): Promise<AudioStream> {
  const res = await fetch(BASE, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload),
  })
  if (!res.ok) throw new Error(`Streams API error: ${res.status}`)
  return res.json()
}

export async function deleteStream(id: string): Promise<void> {
  const res = await fetch(`${BASE}/${id}`, { method: 'DELETE' })
  if (!res.ok) throw new Error(`Streams API error: ${res.status}`)
}
