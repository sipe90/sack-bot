import { AsyncThunkResult } from "@/types"
import { fetchPostJson } from "@/util"

export const PLAY_TTS_REQUEST = "PLAY_TTS_REQUEST"
export const PLAY_TTS_RESOLVED = "PLAY_TTS_RESOLVED"
export const PLAY_TTS_REJECTED = "PLAY_TTS_REJECTED"

export const PLAY_RANDOM_TTS_REQUEST = "PLAY_RANDOM_TTS_REQUEST"
export const PLAY_RANDOM_TTS_RESOLVED = "PLAY_RANDOM_TTS_RESOLVED"
export const PLAY_RANDOM_TTS_REJECTED = "PLAY_RANDOM_TTS_REJECTED"

interface PlayTTSRequestAction {
    type: typeof PLAY_TTS_REQUEST
}

interface PlayTTSResolvedAction {
    type: typeof PLAY_TTS_RESOLVED
}

interface PlayTTSRejectedAction {
    type: typeof PLAY_TTS_REJECTED,
    payload: Error
}

interface PlayRandomTTSRequestAction {
    type: typeof PLAY_RANDOM_TTS_REQUEST
}

interface PlayRandomTTSResolvedAction {
    type: typeof PLAY_RANDOM_TTS_RESOLVED
}

interface PlayRandomTTSRejectedAction {
    type: typeof PLAY_RANDOM_TTS_REJECTED,
    payload: Error
}

export type TTSActions = PlayTTSRequestAction | PlayTTSResolvedAction | PlayTTSRejectedAction |
PlayRandomTTSRequestAction | PlayRandomTTSResolvedAction | PlayRandomTTSRejectedAction

const playTTSRequest = (): PlayTTSRequestAction => ({
    type: PLAY_TTS_REQUEST
})

const playTTSResolved = (): PlayTTSResolvedAction => ({
    type: PLAY_TTS_RESOLVED
})

const playTTSRejected = (error: Error): PlayTTSRejectedAction => ({
    type: PLAY_TTS_REJECTED,
    payload: error
})

export const playTTS = (guildId: string, text: string): AsyncThunkResult => async (dispatch) => {
    try {
        dispatch(playTTSRequest())
        const res = await fetchPostJson<void>(`/api/${guildId}/tts/play`, text)

        if (!res.ok) throw new Error(res.json?.message || res.statusText)

        dispatch(playTTSResolved())
    } catch (error) {
        dispatch(playTTSRejected(error))
    }
}

const playRandomTTSRequest = (): PlayRandomTTSRequestAction => ({
    type: PLAY_RANDOM_TTS_REQUEST
})

const playRandomTTSResolved = (): PlayRandomTTSResolvedAction => ({
    type: PLAY_RANDOM_TTS_RESOLVED
})

const playRandomTTSRejected = (error: Error): PlayRandomTTSRejectedAction => ({
    type: PLAY_RANDOM_TTS_REJECTED,
    payload: error
})

export const playRandomTTS = (guildId: string): AsyncThunkResult => async (dispatch) => {
    try {
        dispatch(playRandomTTSRequest())
        const res = await fetchPostJson<void>(`/api/${guildId}/tts/random`)

        if (!res.ok) throw new Error(res.json?.message || res.statusText)

        dispatch(playRandomTTSResolved())
    } catch (error) {
        dispatch(playRandomTTSRejected(error))
    }
}