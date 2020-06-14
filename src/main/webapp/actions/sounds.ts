import { message } from 'antd'

import { IAudioFile, AsyncThunkResult } from "@/types"
import { fetchGetJson, fetchPostJson, fetchDeleteJson, buildQueryString } from "@/util"

export const FETCH_SOUNDS_REQUEST = "FETCH_SOUNDS_REQUEST"
export const FETCH_SOUNDS_RESOLVED = "FETCH_SOUNDS_RESOLVED"
export const FETCH_SOUNDS_REJECTED = "FETCH_SOUNDS_REJECTED"

export const PLAY_SOUND_REQUEST = "PLAY_SOUND_REQUEST"
export const PLAY_SOUND_RESOLVED = "PLAY_SOUND_RESOLVED"
export const PLAY_SOUND_REJECTED = "PLAY_SOUND_REJECTED"

export const PLAY_RANDOM_SOUND_REQUEST = "PLAY_RANDOM_SOUND_REQUEST"
export const PLAY_RANDOM_SOUND_RESOLVED = "PLAY_RANDOM_SOUND_RESOLVED"
export const PLAY_RANDOM_SOUND_REJECTED = "PLAY_RANDOM_SOUND_REJECTED"

export const PLAY_URL_REQUEST = "PLAY_URL_REQUEST"
export const PLAY_URL_RESOLVED = "PLAY_URL_RESOLVED"
export const PLAY_URL_REJECTED = "PLAY_URL_REJECTED"

export const UPDATE_SOUND_REQUEST = "UPDATE_SOUND_REQUEST"
export const UPDATE_SOUND_RESOLVED = "UPDATE_SOUND_RESOLVED"
export const UPDATE_SOUND_REJECTED = "UPDATE_SOUND_REJECTED"

export const DELETE_SOUND_REQUEST = "DELETE_SOUND_REQUEST"
export const DELETE_SOUND_RESOLVED = "DELETE_SOUND_RESOLVED"
export const DELETE_SOUND_REJECTED = "DELETE_SOUND_REJECTED"

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

interface PlayUrlRequestAction {
    type: typeof PLAY_URL_REQUEST
}

interface PlayUrlResolvedAction {
    type: typeof PLAY_URL_RESOLVED
}

interface PlayUrlRejectedAction {
    type: typeof PLAY_URL_REJECTED,
    payload: Error
}

interface UpdateSoundRequestAction {
    type: typeof UPDATE_SOUND_REQUEST
}

interface UpdateSoundResolvedAction {
    type: typeof UPDATE_SOUND_RESOLVED,
    payload: { guildId: string, name: string, audioFile: IAudioFile }
}

interface UpdateSoundRejectedAction {
    type: typeof UPDATE_SOUND_REJECTED,
    payload: Error
}

interface DeleteSoundRequestAction {
    type: typeof DELETE_SOUND_REQUEST
}

interface DeleteSoundResolvedAction {
    type: typeof DELETE_SOUND_RESOLVED,
    payload: { guildId: string, name: string }
}

interface DeleteSoundRejectedAction {
    type: typeof DELETE_SOUND_REJECTED,
    payload: Error
}

export type SoundsActions = FetchSoundsRequestAction | FetchSoundsResolvedAction | FetchSoundsRejectedAction |
    PlaySoundRequestAction | PlaySoundResolvedAction | PlaySoundRejectedAction |
    PlayRandomSoundRequestAction | PlayRandomSoundResolvedAction | PlayRandomSoundRejectedAction |
    PlayUrlRequestAction | PlayUrlResolvedAction | PlayUrlRejectedAction |
    UpdateSoundRequestAction | UpdateSoundResolvedAction | UpdateSoundRejectedAction |
    DeleteSoundRequestAction | DeleteSoundResolvedAction | DeleteSoundRejectedAction

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
        message.error(`Failed to get sounds: ${error.message}`)
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

export const playSound = (guildId: string, name: string, vol: number): AsyncThunkResult => async (dispatch) => {
    try {
        dispatch(playSoundRequest())
        const res = await fetchPostJson<IAudioFile[]>(`/api/${guildId}/sounds/${name}/play?${buildQueryString({ vol })}`)

        if (!res.ok) throw new Error(res.json?.message || res.statusText)

        dispatch(playSoundResolved())
    } catch (error) {
        message.error(`Failed to play sound: ${error.message}`)
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

export const playRandomSound = (guildId: string, vol: number, tags: string[] = []): AsyncThunkResult => async (dispatch) => {
    try {
        dispatch(playRandomSoundRequest())
        
        const res = await fetchPostJson<IAudioFile[]>(`/api/${guildId}/sounds/rnd?${buildQueryString({ tags, vol })}`)

        if (!res.ok) throw new Error(res.json?.message || res.statusText)

        dispatch(playRandomSoundResolved())
    } catch (error) {
        message.error(`Failed to play sound: ${error.message}`)
        dispatch(playRandomSoundRejected(error))
    }
}

const playUrlRequest = (): PlayUrlRequestAction => ({
    type: PLAY_URL_REQUEST
})

const playUrlResolved = (): PlayUrlResolvedAction => ({
    type: PLAY_URL_RESOLVED
})

const playUrlRejected = (error: Error): PlayUrlRejectedAction => ({
    type: PLAY_URL_REJECTED,
    payload: error
})

export const playUrl = (guildId: string, url: string, vol: number): AsyncThunkResult => async (dispatch) => {
    try {
        dispatch(playUrlRequest())
        
        const res = await fetchPostJson<IAudioFile[]>(`/api/${guildId}/sounds/url?${buildQueryString({ url, vol })}`)

        if (!res.ok) throw new Error(res.json?.message || res.statusText)

        dispatch(playUrlResolved())
    } catch (error) {
        message.error(`Failed to play url: ${error.message}`)
        dispatch(playUrlRejected(error))
    }
}

const updateSoundRequest = (): UpdateSoundRequestAction => ({
    type: UPDATE_SOUND_REQUEST
})

const updateSoundResolved = (guildId: string, name: string, audioFile: IAudioFile): UpdateSoundResolvedAction => ({
    type: UPDATE_SOUND_RESOLVED,
    payload: { guildId, name, audioFile }
})

const updateSoundRejected = (error: Error): UpdateSoundRejectedAction => ({
    type: UPDATE_SOUND_REJECTED,
    payload: error
})

export const updateSound = (guildId: string, name: string, audioFile: IAudioFile): AsyncThunkResult => async (dispatch) => {
    try {
        dispatch(updateSoundRequest())
        const res = await fetchPostJson(`/api/${guildId}/sounds/${name}`, audioFile)

        if (!res.ok) throw new Error(res.json?.message || res.statusText)

        dispatch(updateSoundResolved(guildId, name, audioFile))
        dispatch(fetchSounds(guildId))
    } catch (error) {
        message.error(`Failed to update sound: ${error.message}`)
        dispatch(updateSoundRejected(error))
    }
}

const deleteSoundRequest = (): DeleteSoundRequestAction => ({
    type: DELETE_SOUND_REQUEST
})

const deleteSoundResolved = (guildId: string, name: string): DeleteSoundResolvedAction => ({
    type: DELETE_SOUND_RESOLVED,
    payload: { guildId, name }
})

const deleteSoundRejected = (error: Error): DeleteSoundRejectedAction => ({
    type: DELETE_SOUND_REJECTED,
    payload: error
})

export const deleteSound = (guildId: string, name: string): AsyncThunkResult => async (dispatch) => {
    try {
        dispatch(deleteSoundRequest())
        const res = await fetchDeleteJson(`/api/${guildId}/sounds/${name}`)

        if (!res.ok) throw new Error(res.json?.message || res.statusText)

        dispatch(deleteSoundResolved(guildId, name))
        dispatch(fetchSounds(guildId))
    } catch (error) {
        message.error(`Failed to delete sound: ${error.message}`)
        dispatch(deleteSoundRejected(error))
    }
}