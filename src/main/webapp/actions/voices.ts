
import { IVoiceLines, ActionGroup } from "@/types"
import { fetchGetJson, fetchPostJson, apiThunk } from "@/util"

export const FETCH_VOICE_LINES_REQUEST = "FETCH_VOICE_LINES_REQUEST"
export const FETCH_VOICE_LINES_RESOLVED = "FETCH_VOICE_LINES_RESOLVED"
export const FETCH_VOICE_LINES_REJECTED = "FETCH_VOICE_LINES_REJECTED"

export const PLAY_VOICE_LINES_REQUEST = "PLAY_VOICE_LINES_REQUEST"
export const PLAY_VOICE_LINES_RESOLVED = "PLAY_VOICE_LINES_RESOLVED"
export const PLAY_VOICE_LINES_REJECTED = "PLAY_VOICE_LINES_REJECTED"

export type VoicesActions = ActionGroup<typeof FETCH_VOICE_LINES_REQUEST, typeof FETCH_VOICE_LINES_RESOLVED, typeof FETCH_VOICE_LINES_REJECTED, IVoiceLines>
    | ActionGroup<typeof PLAY_VOICE_LINES_REQUEST, typeof PLAY_VOICE_LINES_RESOLVED, typeof PLAY_VOICE_LINES_REJECTED>

export const fetchVoiceLines = () => apiThunk({
    types: [FETCH_VOICE_LINES_REQUEST, FETCH_VOICE_LINES_RESOLVED, FETCH_VOICE_LINES_REJECTED],
    apiCall: () => fetchGetJson<IVoiceLines[]>(`/api/voices`)
})

export const playVoiceLines = (guildId: string, voice: string, voiceLines: string[]) => apiThunk({
    types: [PLAY_VOICE_LINES_REQUEST, PLAY_VOICE_LINES_RESOLVED, PLAY_VOICE_LINES_REJECTED],
    apiCall: () => fetchPostJson(`/api/${guildId}/voices/play?voice=${voice}`, voiceLines)
})
