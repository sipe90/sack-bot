import { message } from 'antd'

import history from '@/history'
import { IMembership, IGuild, AsyncThunkResult, IGuildMember } from "@/types"
import { buildQueryString, fetchGetJson, fetchPutJson } from "@/util"
import { fetchSounds } from "@/actions/sounds"

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

interface FetchUserRequestAction {
    type: typeof FETCH_USER_REQUEST
}

interface FetchUserResolvedAction {
    type: typeof FETCH_USER_RESOLVED,
    payload: IMembership[]
}

interface FetchUserRejectedAction {
    type: typeof FETCH_USER_REJECTED,
    payload: Error
}

interface FetchGuildsRequestAction {
    type: typeof FETCH_GUILDS_REQUEST
}

interface FetchGuildsResolvedAction {
    type: typeof FETCH_GUILDS_RESOLVED,
    payload: IGuild[]
}

interface FetchGuildsRejectedAction {
    type: typeof FETCH_GUILDS_REJECTED,
    payload: Error
}


interface FetchGuildMembersRequestAction {
    type: typeof FETCH_GUILD_MEMBERS_REQUEST
}

interface FetchGuildMembersResolvedAction {
    type: typeof FETCH_GUILD_MEMBERS_RESOLVED,
    payload: { guildId: string, members: IGuildMember[] }
}

interface FetchGuildMembersRejectedAction {
    type: typeof FETCH_GUILD_MEMBERS_REJECTED,
    payload: Error
}

interface UpdateEntrySoundRequestAction {
    type: typeof UPDATE_ENTRY_SOUND_REQUEST
}

interface UpdateEntrySoundResolvedAction {
    type: typeof UPDATE_ENTRY_SOUND_RESOLVED
}

interface UpdateEntrySoundRejectedAction {
    type: typeof UPDATE_ENTRY_SOUND_REJECTED,
    payload: Error
}

interface UpdateExitSoundRequestAction {
    type: typeof UPDATE_EXIT_SOUND_REQUEST
}

interface UpdateExitSoundResolvedAction {
    type: typeof UPDATE_EXIT_SOUND_RESOLVED
}

interface UpdateExitSoundRejectedAction {
    type: typeof UPDATE_EXIT_SOUND_REJECTED,
    payload: Error
}

interface SelectGuildAction {
    type: typeof SELECT_GUILD,
    payload: string
}

export type UserActions = FetchUserRequestAction | FetchUserResolvedAction | FetchUserRejectedAction |
    FetchGuildsRequestAction | FetchGuildsResolvedAction | FetchGuildsRejectedAction |
    FetchGuildMembersRequestAction | FetchGuildMembersResolvedAction | FetchGuildMembersRejectedAction |
    UpdateEntrySoundRequestAction | UpdateEntrySoundResolvedAction | UpdateEntrySoundRejectedAction |
    UpdateExitSoundRequestAction | UpdateExitSoundResolvedAction | UpdateExitSoundRejectedAction |
    SelectGuildAction

const fetchUserRequest = (): FetchUserRequestAction => ({
    type: FETCH_USER_REQUEST
})

const fetchUserResolved = (memberships: IMembership[]): FetchUserResolvedAction => ({
    type: FETCH_USER_RESOLVED,
    payload: memberships
})


const fetchUserRejected = (error: Error): FetchUserRejectedAction => ({
    type: FETCH_USER_REJECTED,
    payload: error
})

export const fetchUser = (): AsyncThunkResult => async (dispatch) => {
    try {
        dispatch(fetchUserRequest())
        const res = await fetchGetJson<IMembership[]>('/api/me')

        if (res.status === 401) {
            dispatch(fetchUserRejected(new Error('Unauthorized')))
            history.push('/login')
            return
        }

        if (!res.ok) throw new Error(res.json?.message || res.statusText)

        dispatch(fetchUserResolved(res.json))
    } catch (error) {
        message.error(`Failed to get user details: ${error.message}`)
        dispatch(fetchUserRejected(error))
    }
}

const fetchGuildsRequest = (): FetchGuildsRequestAction => ({
    type: FETCH_GUILDS_REQUEST
})

const fetchGuildsResolved = (guilds: IGuild[]): FetchGuildsResolvedAction => ({
    type: FETCH_GUILDS_RESOLVED,
    payload: guilds
})

const fetchGuildsRejected = (error: Error): FetchGuildsRejectedAction => ({
    type: FETCH_GUILDS_REJECTED,
    payload: error
})

export const fetchGuilds = (): AsyncThunkResult => async (dispatch) => {
    try {
        dispatch(fetchGuildsRequest())
        const res = await fetchGetJson<IGuild[]>('/api/guilds')

        if (res.status === 401) {
            dispatch(fetchGuildsRejected(new Error('Unauthorized')))
            history.push('/login')
            return
        }

        if (!res.ok) throw new Error(res.json?.message || res.statusText)

        dispatch(fetchGuildsResolved(res.json))

        if (res.json.length) {
            const selectedGuild = res.json[0]
            dispatch(selectGuild(selectedGuild.id))
        }
    } catch (error) {
        message.error(`Failed to get user guilds: ${error.message}`)
        dispatch(fetchGuildsRejected(error))
    }
}

const fetchGuildMembersRequest = (): FetchGuildMembersRequestAction => ({
    type: FETCH_GUILD_MEMBERS_REQUEST
})

const fetchGuildMembersResolved = (guildId: string, members: IGuildMember[]): FetchGuildMembersResolvedAction => ({
    type: FETCH_GUILD_MEMBERS_RESOLVED,
    payload: { guildId, members }
})

const fetchGuildMembersRejected = (error: Error): FetchGuildMembersRejectedAction => ({
    type: FETCH_GUILD_MEMBERS_REJECTED,
    payload: error
})

export const fetchGuildMembers = (guildId: string): AsyncThunkResult => async (dispatch) => {
    try {
        dispatch(fetchGuildMembersRequest())
        const res = await fetchGetJson<IGuildMember[]>(`/api/${guildId}/members`)

        if (res.status === 401) {
            dispatch(fetchGuildMembersRejected(new Error('Unauthorized')))
            history.push('/login')
            return
        }

        if (!res.ok) throw new Error(res.json?.message || res.statusText)

        dispatch(fetchGuildMembersResolved(guildId, res.json))
    } catch (error) {
        message.error(`Failed to get guild members: ${error.message}`)
        dispatch(fetchGuildMembersRejected(error))
    }
}

const updateEntrySoundRequest = (): UpdateEntrySoundRequestAction => ({
    type: UPDATE_ENTRY_SOUND_REQUEST
})

const updateEntrySoundResolved = (): UpdateEntrySoundResolvedAction => ({
    type: UPDATE_ENTRY_SOUND_RESOLVED
})

const updateEntrySoundRejected = (error: Error): UpdateEntrySoundRejectedAction => ({
    type: UPDATE_ENTRY_SOUND_REJECTED,
    payload: error
})

export const updateEntrySound = (guildId: string, name?: string): AsyncThunkResult => async (dispatch) => {
    try {
        dispatch(updateEntrySoundRequest())
        const res = await fetchPutJson<IGuildMember[]>(`/api/${guildId}/sounds/entry?${buildQueryString({ name })}`)

        if (res.status === 401) {
            dispatch(updateEntrySoundRejected(new Error('Unauthorized')))
            history.push('/login')
            return
        }

        if (!res.ok) throw new Error(res.json?.message || res.statusText)

        dispatch(updateEntrySoundResolved())
        await dispatch(fetchUser())
    } catch (error) {
        message.error(`Failed to update entry sound: ${error.message}`)
        dispatch(updateEntrySoundRejected(error))
    }
}

const updateExitSoundRequest = (): UpdateExitSoundRequestAction => ({
    type: UPDATE_EXIT_SOUND_REQUEST
})

const updateExitSoundResolved = (): UpdateExitSoundResolvedAction => ({
    type: UPDATE_EXIT_SOUND_RESOLVED
})

const updateExitSoundRejected = (error: Error): UpdateExitSoundRejectedAction => ({
    type: UPDATE_EXIT_SOUND_REJECTED,
    payload: error
})

export const updateExitSound = (guildId: string, name?: string): AsyncThunkResult => async (dispatch) => {
    try {
        dispatch(updateExitSoundRequest())
        const res = await fetchPutJson<IGuildMember[]>(`/api/${guildId}/sounds/exit?${buildQueryString({ name })}`)

        if (res.status === 401) {
            dispatch(updateExitSoundRejected(new Error('Unauthorized')))
            history.push('/login')
            return
        }

        if (!res.ok) throw new Error(res.json?.message || res.statusText)

        dispatch(updateExitSoundResolved())
        await dispatch(fetchUser())
    } catch (error) {
        message.error(`Failed to update exit sound: ${error.message}`)
        dispatch(updateExitSoundRejected(error))
    }
}

export const selectGuild = (guildId: string): SelectGuildAction => ({
    type: SELECT_GUILD,
    payload: guildId
})