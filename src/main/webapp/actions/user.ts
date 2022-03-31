import { IGuild, IGuildMember, AppDispatch, ActionGroup, AsyncThunk, UserInfo } from "@/types"
import { buildQueryString, fetchGetJson, fetchPutJson, apiThunk } from "@/util"
import { enqueueErrorSnackbar } from './snackbar'

export const FETCH_USER_REQUEST = "FETCH_USER_REQUEST"
export const FETCH_USER_RESOLVED = "FETCH_USER_RESOLVED"
export const FETCH_USER_REJECTED = "FETCH_USER_REJECTED"

export const FETCH_GUILDS_REQUEST = "FETCH_GUILDS_REQUEST"
export const FETCH_GUILDS_RESOLVED = "FETCH_GUILDS_RESOLVED"
export const FETCH_GUILDS_REJECTED = "FETCH_GUILDS_REJECTED"

export const FETCH_GUILD_MEMBERS_REQUEST = "FETCH_GUILD_MEMBERS_REQUEST"
export const FETCH_GUILD_MEMBERS_RESOLVED = "FETCH_GUILD_MEMBERS_RESOLVED"
export const FETCH_GUILD_MEMBERS_REJECTED = "FETCH_GUILD_MEMBERS_REJECTED"

export const UPDATE_ENTRY_SOUND_REQUEST = "UPDATE_ENTRY_SOUND_REQUEST"
export const UPDATE_ENTRY_SOUND_RESOLVED = "UPDATE_ENTRY_SOUND_RESOLVED"
export const UPDATE_ENTRY_SOUND_REJECTED = "UPDATE_ENTRY_SOUND_REJECTED"

export const UPDATE_EXIT_SOUND_REQUEST = "UPDATE_EXIT_SOUND_REQUEST"
export const UPDATE_EXIT_SOUND_RESOLVED = "UPDATE_EXIT_SOUND_RESOLVED"
export const UPDATE_EXIT_SOUND_REJECTED = "UPDATE_EXIT_SOUND_REJECTED"

export const SELECT_GUILD = "SELECT_GUILD"

interface SelectGuildAction {
    type: typeof SELECT_GUILD,
    payload: string
}

export type UserActions = ActionGroup<typeof FETCH_USER_REQUEST, typeof FETCH_USER_RESOLVED, typeof FETCH_USER_REJECTED, UserInfo>
    | ActionGroup<typeof FETCH_GUILDS_REQUEST, typeof FETCH_GUILDS_RESOLVED, typeof FETCH_GUILDS_REJECTED, IGuild[]>
    | ActionGroup<typeof FETCH_GUILD_MEMBERS_REQUEST, typeof FETCH_GUILD_MEMBERS_RESOLVED, typeof FETCH_GUILD_MEMBERS_REJECTED, { guildId: string, members: IGuildMember[] }>
    | ActionGroup<typeof UPDATE_ENTRY_SOUND_REQUEST, typeof UPDATE_ENTRY_SOUND_RESOLVED, typeof UPDATE_ENTRY_SOUND_REJECTED>
    | ActionGroup<typeof UPDATE_EXIT_SOUND_REQUEST, typeof UPDATE_EXIT_SOUND_RESOLVED, typeof UPDATE_EXIT_SOUND_REJECTED>
    | SelectGuildAction

export const fetchUser = (): AsyncThunk => async (dispatch) => {
    dispatch(apiThunk({
        types: [FETCH_USER_REQUEST, FETCH_USER_RESOLVED, FETCH_USER_REJECTED],
        apiCall: () => fetchGetJson<UserInfo>(`/api/me`),
        onError: (err, dispatch) => dispatch(enqueueErrorSnackbar(`Failed to load user info: ${err.message}`))
    }))
}

export const fetchGuilds = (): AsyncThunk => async (dispatch, getState) => {
    await dispatch(apiThunk({
        types: [FETCH_GUILDS_REQUEST, FETCH_GUILDS_RESOLVED, FETCH_GUILDS_REJECTED],
        apiCall: () => fetchGetJson<IGuild[]>(`/api/guilds`),
        onError: (err, dispatch) => dispatch(enqueueErrorSnackbar(`Failed to load guilds: ${err.message}`))
    }))
    const selectedGuild = getState().user.guilds[0]
    dispatch(selectGuild(selectedGuild.id))
}

export const fetchGuildMembers = (guildId: string) => apiThunk({
    types: [FETCH_GUILD_MEMBERS_REQUEST, FETCH_GUILD_MEMBERS_RESOLVED, FETCH_GUILD_MEMBERS_REJECTED],
    apiCall: () => fetchGetJson<IGuildMember[]>(`/api/${guildId}/members`),
    onError: (err, dispatch) => dispatch(enqueueErrorSnackbar(`Failed to load guild members: ${err.message}`)),
    responseMapper: (members) => ({ guildId, members })
})

export const updateEntrySound = (guildId: string, name?: string): AsyncThunk => async (dispatch) => {
    await dispatch(apiThunk({
        types: [UPDATE_ENTRY_SOUND_REQUEST, UPDATE_ENTRY_SOUND_RESOLVED, UPDATE_ENTRY_SOUND_REJECTED],
        apiCall: () => fetchPutJson(`/api/${guildId}/sounds/entry?${buildQueryString({ name })}`),
        onError: (err, dispatch) => dispatch(enqueueErrorSnackbar(`Failed to update entry sound: ${err.message}`))
    }))
    dispatch(fetchUser())
}

export const updateExitSound = (guildId: string, name?: string) => async (dispatch: AppDispatch) => {
    await dispatch(apiThunk({
        types: [UPDATE_EXIT_SOUND_REQUEST, UPDATE_EXIT_SOUND_RESOLVED, UPDATE_EXIT_SOUND_REJECTED],
        apiCall: () => fetchPutJson(`/api/${guildId}/sounds/exit?${buildQueryString({ name })}`),
        onError: (err, dispatch) => dispatch(enqueueErrorSnackbar(`Failed to update exit sound: ${err.message}`))
    }))
    dispatch(fetchUser())
}

export const selectGuild = (guildId: string): SelectGuildAction => ({
    type: SELECT_GUILD,
    payload: guildId
})