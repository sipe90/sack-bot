import { message } from 'antd'

import history from '@/history'
import { AsyncThunkResult } from "@/types"
import { fetchPostJson, fetchGetJson, buildQueryString } from "@/util"

export const GET_VOICES_REQUEST = "GET_VOICES_REQUEST"
export const GET_VOICES_RESOLVED = "GET_VOICES_RESOLVED"
export const GET_VOICES_REJECTED = "GET_VOICES_REJECTED"

export const PLAY_TTS_REQUEST = "PLAY_TTS_REQUEST"
export const PLAY_TTS_RESOLVED = "PLAY_TTS_RESOLVED"
export const PLAY_TTS_REJECTED = "PLAY_TTS_REJECTED"

export const PLAY_RANDOM_TTS_REQUEST = "PLAY_RANDOM_TTS_REQUEST"
export const PLAY_RANDOM_TTS_RESOLVED = "PLAY_RANDOM_TTS_RESOLVED"
export const PLAY_RANDOM_TTS_REJECTED = "PLAY_RANDOM_TTS_REJECTED"

interface GetVoicesRequestAction {
    type: typeof GET_VOICES_REQUEST
}

interface GetVoicesResolvedAction {
    type: typeof GET_VOICES_RESOLVED
    payload: string[]
}

interface GetVoicesRejectedAction {
    type: typeof GET_VOICES_REJECTED,
    payload: Error
}

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

export type TTSActions = GetVoicesRequestAction | GetVoicesResolvedAction | GetVoicesRejectedAction |
    PlayTTSRequestAction | PlayTTSResolvedAction | PlayTTSRejectedAction |
    PlayRandomTTSRequestAction | PlayRandomTTSResolvedAction | PlayRandomTTSRejectedAction

const getVoicesRequest = (): GetVoicesRequestAction => ({
    type: GET_VOICES_REQUEST
})

const getVoicesResolved = (voices: string[]): GetVoicesResolvedAction => ({
    type: GET_VOICES_RESOLVED,
    payload: voices
})

const getVoicesRejected = (error: Error): GetVoicesRejectedAction => ({
    type: GET_VOICES_REJECTED,
    payload: error
})

export const getVoices = (guildId: string): AsyncThunkResult => async (dispatch) => {
    try {
        dispatch(getVoicesRequest())
        const res = await fetchGetJson<string[]>(`/api/${guildId}/tts`)

        if (res.status === 401) {
            dispatch(playTTSRejected(new Error('Unauthorized')))
            history.push('/login')
            return
        }

        if (!res.ok) throw new Error(res.json?.message || res.statusText)

        dispatch(getVoicesResolved(res.json))
    } catch (error) {
        message.error(`Failed fetch TTS voices: ${error.message}`)
        dispatch(getVoicesRejected(error))
    }
}

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

export const playTTS = (guildId: string, voice: string, text: string): AsyncThunkResult => async (dispatch) => {
    try {
        dispatch(playTTSRequest())
        const res = await fetchPostJson<void>(`/api/${guildId}/tts/play?${buildQueryString({ voice })}`, text)

        if (res.status === 401) {
            dispatch(playTTSRejected(new Error('Unauthorized')))
            history.push('/login')
            return
        }

        if (!res.ok) throw new Error(res.json?.message || res.statusText)

        dispatch(playTTSResolved())
    } catch (error) {
        message.error(`Failed to play TTS: ${error.message}`)
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

export const playRandomTTS = (guildId: string, voice: string): AsyncThunkResult => async (dispatch) => {
    try {
        dispatch(playRandomTTSRequest())
        const res = await fetchPostJson<void>(`/api/${guildId}/tts/random?${buildQueryString({ voice })}`)

        if (res.status === 401) {
            dispatch(playRandomTTSRejected(new Error('Unauthorized')))
            history.push('/login')
            return
        }

        if (!res.ok) throw new Error(res.json?.message || res.statusText)

        dispatch(playRandomTTSResolved())
    } catch (error) {
        message.error(`Failed to play TTS: ${error.message}`)
        dispatch(playRandomTTSRejected(error))
    }
}