import { IAudioFile, AsyncThunkResult } from "@/types"
import { fetchGetJson, fetchPostJson } from "@/util"

export const FETCH_SOUNDS_REQUEST = "FETCH_SOUNDS_REQUEST"
export const FETCH_SOUNDS_RESOLVED = "FETCH_SOUNDS_RESOLVED"
export const FETCH_SOUNDS_REJECTED = "FETCH_SOUNDS_REJECTED"

export const PLAY_SOUND_REQUEST = "PLAY_SOUND_REQUEST"
export const PLAY_SOUND_RESOLVED = "PLAY_SOUND_RESOLVED"
export const PLAY_SOUND_REJECTED = "PLAY_SOUND_REJECTED"

export const PLAY_RANDOM_SOUND_REQUEST = "PLAY_RANDOM_SOUND_REQUEST"
export const PLAY_RANDOM_SOUND_RESOLVED = "PLAY_RANDOM_SOUND_RESOLVED"
export const PLAY_RANDOM_SOUND_REJECTED = "PLAY_RANDOM_SOUND_REJECTED"

interface FetchSoundsRequestAction {
    type: typeof FETCH_SOUNDS_REQUEST
}

interface FetchSoundsResolvedAction {
    type: typeof FETCH_SOUNDS_RESOLVED,
    payload: IAudioFile[]
}

interface FetchSoundsRejectedAction {
    type: typeof FETCH_SOUNDS_REJECTED,
    payload: Error
}

interface PlaySoundRequestAction {
    type: typeof PLAY_SOUND_REQUEST
}

interface PlaySoundResolvedAction {
    type: typeof PLAY_SOUND_RESOLVED
}

interface PlaySoundRejectedAction {
    type: typeof PLAY_SOUND_REJECTED,
    payload: Error
}

interface PlayRandomSoundRequestAction {
    type: typeof PLAY_RANDOM_SOUND_REQUEST
}

interface PlayRandomSoundResolvedAction {
    type: typeof PLAY_RANDOM_SOUND_RESOLVED
}

interface PlayRandomSoundRejectedAction {
    type: typeof PLAY_RANDOM_SOUND_REJECTED,
    payload: Error
}

export type SoundsActions = FetchSoundsRequestAction | FetchSoundsResolvedAction | FetchSoundsRejectedAction |
    PlaySoundRequestAction | PlaySoundResolvedAction | PlaySoundRejectedAction |
    PlayRandomSoundRequestAction | PlayRandomSoundResolvedAction | PlayRandomSoundRejectedAction

const fetchSoundsRequest = (): FetchSoundsRequestAction => ({
    type: FETCH_SOUNDS_REQUEST
})

const fetchSoundsResolved = (audioFiles: IAudioFile[]): FetchSoundsResolvedAction => ({
    type: FETCH_SOUNDS_RESOLVED,
    payload: audioFiles
})

const fetchSoundsRejected = (error: Error): FetchSoundsRejectedAction => ({
    type: FETCH_SOUNDS_REJECTED,
    payload: error
})

export const fetchSounds = (guildId: string): AsyncThunkResult => async (dispatch) => {
    try {
        dispatch(fetchSoundsRequest())
        const res = await fetchGetJson<IAudioFile[]>(`/api/${guildId}/sounds`)

        if (!res.ok) throw new Error(res.json?.message || res.statusText)

        dispatch(fetchSoundsResolved(res.json))
    } catch (error) {
        dispatch(fetchSoundsRejected(error))
    }
}

const playSoundRequest = (): PlaySoundRequestAction => ({
    type: PLAY_SOUND_REQUEST
})

const playSoundResolved = (): PlaySoundResolvedAction => ({
    type: PLAY_SOUND_RESOLVED
})

const playSoundRejected = (error: Error): PlaySoundRejectedAction => ({
    type: PLAY_SOUND_REJECTED,
    payload: error
})

export const playSound = (guildId: string, name: string): AsyncThunkResult => async (dispatch) => {
    try {
        dispatch(playSoundRequest())
        const res = await fetchPostJson<IAudioFile[]>(`/api/${guildId}/sounds/${name}/play`)

        if (!res.ok) throw new Error(res.json?.message || res.statusText)

        dispatch(playSoundResolved())
    } catch (error) {
        dispatch(playSoundRejected(error))
    }
}

const playRandomSoundRequest = (): PlayRandomSoundRequestAction => ({
    type: PLAY_RANDOM_SOUND_REQUEST
})

const playRandomSoundResolved = (): PlayRandomSoundResolvedAction => ({
    type: PLAY_RANDOM_SOUND_RESOLVED
})

const playRandomSoundRejected = (error: Error): PlayRandomSoundRejectedAction => ({
    type: PLAY_RANDOM_SOUND_REJECTED,
    payload: error
})

export const playRandomSound = (guildId: string): AsyncThunkResult => async (dispatch) => {
    try {
        dispatch(playRandomSoundRequest())
        const res = await fetchPostJson<IAudioFile[]>(`/api/${guildId}/sounds/rnd`)

        if (!res.ok) throw new Error(res.json?.message || res.statusText)

        dispatch(playRandomSoundResolved())
    } catch (error) {
        dispatch(playRandomSoundRejected(error))
    }
}