import { ActionGroup, IAudioFile, AsyncThunk } from "@/types"
import { fetchGetJson, fetchPostJson, fetchDeleteJson, buildQueryString, apiThunk } from "@/util"
import { enqueueErrorSnackbar } from '@/actions/snackbar'

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

export type SoundsActions = ActionGroup<typeof FETCH_SOUNDS_REQUEST, typeof FETCH_SOUNDS_RESOLVED, typeof FETCH_SOUNDS_REJECTED, IAudioFile[]>
    | ActionGroup<typeof PLAY_SOUND_REQUEST, typeof PLAY_SOUND_RESOLVED, typeof PLAY_SOUND_REJECTED>
    | ActionGroup<typeof PLAY_RANDOM_SOUND_REQUEST, typeof PLAY_RANDOM_SOUND_RESOLVED, typeof PLAY_RANDOM_SOUND_REJECTED>
    | ActionGroup<typeof PLAY_URL_REQUEST, typeof PLAY_URL_RESOLVED, typeof PLAY_URL_REJECTED>
    | ActionGroup<typeof UPDATE_SOUND_REQUEST, typeof UPDATE_SOUND_RESOLVED, typeof UPDATE_SOUND_REJECTED>
    | ActionGroup<typeof DELETE_SOUND_REQUEST, typeof DELETE_SOUND_RESOLVED, typeof DELETE_SOUND_REJECTED>



export const fetchSounds = (guildId: string) => apiThunk({
    types: [FETCH_SOUNDS_REQUEST, FETCH_SOUNDS_RESOLVED, FETCH_SOUNDS_REJECTED],
    apiCall: () => fetchGetJson<IAudioFile[]>(`/api/${guildId}/sounds`),
    onError: (err, dispatch) => dispatch(enqueueErrorSnackbar(`Failed to load sounds: ${err.message}`))
})

export const playSound = (guildId: string, name: string, vol?: number) => apiThunk({
    types: [PLAY_SOUND_REQUEST, PLAY_SOUND_RESOLVED, PLAY_SOUND_REJECTED],
    apiCall: () => fetchPostJson(`/api/${guildId}/sounds/${name}/play?${buildQueryString({ vol })}`),
    onError: (err, dispatch) => dispatch(enqueueErrorSnackbar(`Failed to play sound: ${err.message}`))
})

export const playRandomSound = (guildId: string, vol: number, tags: string[] = []) => apiThunk({
    types: [PLAY_RANDOM_SOUND_REQUEST, PLAY_RANDOM_SOUND_RESOLVED, PLAY_RANDOM_SOUND_REJECTED],
    apiCall: () => fetchPostJson(`/api/${guildId}/sounds/rnd?${buildQueryString({ tags, vol })}`),
    onError: (err, dispatch) => dispatch(enqueueErrorSnackbar(`Failed to play sound: ${err.message}`))
})

export const playUrl = (guildId: string, url: string, vol: number) => apiThunk({
    types: [PLAY_URL_REQUEST, PLAY_URL_RESOLVED, PLAY_URL_REJECTED],
    apiCall: () => fetchPostJson(`/api/${guildId}/sounds/url?${buildQueryString({ url, vol })}`),
    onError: (err, dispatch) => dispatch(enqueueErrorSnackbar(`Failed to play from URL: ${err.message}`))
})

export const updateSound = (guildId: string, name: string, audioFile: IAudioFile): AsyncThunk => async (dispatch) => {
    await dispatch(apiThunk({
        types: [UPDATE_SOUND_REQUEST, UPDATE_SOUND_RESOLVED, UPDATE_SOUND_REJECTED],
        apiCall: () => fetchPostJson(`/api/${guildId}/sounds/${name}`, audioFile),
        onError: (err, dispatch) => dispatch(enqueueErrorSnackbar(`Failed to update sound: ${err.message}`))
    }))
    dispatch(fetchSounds(guildId))
}

export const deleteSound = (guildId: string, name: string): AsyncThunk => async (dispatch) => {
    await dispatch(apiThunk({
        types: [DELETE_SOUND_REQUEST, DELETE_SOUND_RESOLVED, DELETE_SOUND_REJECTED],
        apiCall: () => fetchDeleteJson(`/api/${guildId}/sounds/${name}`),
        onError: (err, dispatch) => dispatch(enqueueErrorSnackbar(`Failed to delete sound: ${err.message}`))
    }))
    dispatch(fetchSounds(guildId))
}