import { fetchPostJson, buildQueryString, apiThunk } from "@/util"
import { ActionGroup } from '@/types'

export const FETCH_VOICES_REQUEST = "FETCH_VOICES_REQUEST"
export const FETCH_VOICES_RESOLVED = "FETCH_VOICES_RESOLVED"
export const FETCH_VOICES_REJECTED = "FETCH_VOICES_REJECTED"

export const PLAY_TTS_REQUEST = "PLAY_TTS_REQUEST"
export const PLAY_TTS_RESOLVED = "PLAY_TTS_RESOLVED"
export const PLAY_TTS_REJECTED = "PLAY_TTS_REJECTED"

export const PLAY_RANDOM_TTS_REQUEST = "PLAY_RANDOM_TTS_REQUEST"
export const PLAY_RANDOM_TTS_RESOLVED = "PLAY_RANDOM_TTS_RESOLVED"
export const PLAY_RANDOM_TTS_REJECTED = "PLAY_RANDOM_TTS_REJECTED"

export type TTSActions = ActionGroup<typeof PLAY_TTS_REQUEST, typeof PLAY_TTS_RESOLVED, typeof PLAY_TTS_REJECTED>
    | ActionGroup<typeof PLAY_RANDOM_TTS_REQUEST, typeof PLAY_RANDOM_TTS_RESOLVED, typeof PLAY_RANDOM_TTS_REJECTED>

export const playTTS = (guildId: string, voice: string, text: string) =>
    apiThunk({
        types: [PLAY_TTS_REQUEST, PLAY_TTS_RESOLVED, PLAY_TTS_REJECTED],
        apiCall: () => fetchPostJson(`/api/${guildId}/tts/play?${buildQueryString({ voice })}`, text)
    })

export const playRandomTTS = (guildId: string, voice: string) =>
    apiThunk({
        types: [PLAY_RANDOM_TTS_REQUEST, PLAY_RANDOM_TTS_RESOLVED, PLAY_RANDOM_TTS_REJECTED],
        apiCall: () => fetchPostJson(`/api/${guildId}/tts/random?${buildQueryString({ voice })}`)
    })