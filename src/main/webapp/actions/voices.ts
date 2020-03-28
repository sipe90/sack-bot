import { message } from 'antd'

import { AsyncThunkResult, IVoiceLines } from "@/types"
import { fetchGetJson, fetchPostJson } from "@/util"

export const FETCH_VOICE_LINES_REQUEST = "FETCH_VOICE_LINES_REQUEST"
export const FETCH_VOICE_LINES_RESOLVED = "FETCH_VOICE_LINES_RESOLVED"
export const FETCH_VOICE_LINES_REJECTED = "FETCH_VOICE_LINES_REJECTED"

export const PLAY_VOICE_LINES_REQUEST = "PLAY_VOICE_LINES_REQUEST"
export const PLAY_VOICE_LINES_RESOLVED = "PLAY_VOICE_LINES_RESOLVED"
export const PLAY_VOICE_LINES_REJECTED = "PLAY_VOICE_LINES_REJECTED"

interface FetchVoiceLinesRequestAction {
    type: typeof FETCH_VOICE_LINES_REQUEST
}

interface FetchVoiceLinesResolvedAction {
    type: typeof FETCH_VOICE_LINES_RESOLVED,
    payload: IVoiceLines
}

interface FetchVoiceLinesRejectedAction {
    type: typeof FETCH_VOICE_LINES_REJECTED,
    payload: Error
}

interface PlayVoiceLinesRequestAction {
    type: typeof PLAY_VOICE_LINES_REQUEST
}

interface PlayVoiceLinesResolvedAction {
    type: typeof PLAY_VOICE_LINES_RESOLVED
}

interface PlayVoiceLinesRejectedAction {
    type: typeof PLAY_VOICE_LINES_REJECTED,
    payload: Error
}

export type VoicesActions = FetchVoiceLinesRequestAction | FetchVoiceLinesResolvedAction | FetchVoiceLinesRejectedAction |
PlayVoiceLinesRequestAction | PlayVoiceLinesResolvedAction | PlayVoiceLinesRejectedAction

const fetchVoiceLinesRequest = (): FetchVoiceLinesRequestAction => ({
    type: FETCH_VOICE_LINES_REQUEST
})

const fetchVoiceLinesResolved = (voiceLines: IVoiceLines): FetchVoiceLinesResolvedAction => ({
    type: FETCH_VOICE_LINES_RESOLVED,
    payload: voiceLines
})

const fetchVoiceLinesRejected = (error: Error): FetchVoiceLinesRejectedAction => ({
    type: FETCH_VOICE_LINES_REJECTED,
    payload: error
})

export const fetchVoiceLines = (): AsyncThunkResult => async (dispatch) => {
    try {
        dispatch(fetchVoiceLinesRequest())
        const res = await fetchGetJson<IVoiceLines>(`/api/voices`)

        if (!res.ok) throw new Error(res.json?.message || res.statusText)

        dispatch(fetchVoiceLinesResolved(res.json))
    } catch (error) {
        message.error(`Failed to get voice lines: ${error.message}`)
        dispatch(fetchVoiceLinesRejected(error))
    }
}

const playVoiceLinesRequest = (): PlayVoiceLinesRequestAction => ({
    type: PLAY_VOICE_LINES_REQUEST
})

const playVoiceLinesResolved = (): PlayVoiceLinesResolvedAction => ({
    type: PLAY_VOICE_LINES_RESOLVED
})

const playVoiceLinesRejected = (error: Error): PlayVoiceLinesRejectedAction => ({
    type: PLAY_VOICE_LINES_REJECTED,
    payload: error
})

export const playVoiceLines = (guildId: string, voice: string, voiceLines: string[]): AsyncThunkResult => async (dispatch) => {
    try {
        dispatch(playVoiceLinesRequest())
        const res = await fetchPostJson<void>(`/api/${guildId}/voices/play?voice=${voice}`, voiceLines)

        if (!res.ok) throw new Error(res.json?.message || res.statusText)

        dispatch(playVoiceLinesResolved())
    } catch (error) {
        message.error(`Failed to play voice lines: ${error.message}`)
        dispatch(playVoiceLinesRejected(error))
    }
}