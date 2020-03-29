import { message } from 'antd'

import { IMembership, IGuild, AsyncThunkResult, IGuildMember } from "@/types"
import { fetchGetJson } from "@/util"
import { fetchSounds } from "./sounds"

export const FETCH_USER_REQUEST = "FETCH_USER_REQUEST"
export const FETCH_USER_RESOLVED = "FETCH_USER_RESOLVED"
export const FETCH_USER_REJECTED = "FETCH_USER_REJECTED"

export const FETCH_GUILDS_REQUEST = "FETCH_GUILDS_REQUEST"
export const FETCH_GUILDS_RESOLVED = "FETCH_GUILDS_RESOLVED"
export const FETCH_GUILDS_REJECTED = "FETCH_GUILDS_REJECTED"

export const FETCH_GUILD_MEMBERS_REQUEST = "FETCH_GUILD_MEMBERS_REQUEST"
export const FETCH_GUILD_MEMBERS_RESOLVED = "FETCH_GUILD_MEMBERS_RESOLVED"
export const FETCH_GUILD_MEMBERS_REJECTED = "FETCH_GUILD_MEMBERS_REJECTED"

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

interface SelectGuildAction {
    type: typeof SELECT_GUILD,
    payload: string
}

export type UserActions = FetchUserRequestAction | FetchUserResolvedAction | FetchUserRejectedAction | 
    FetchGuildsRequestAction | FetchGuildsResolvedAction | FetchGuildsRejectedAction |
    FetchGuildMembersRequestAction | FetchGuildMembersResolvedAction | FetchGuildMembersRejectedAction |
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

        if (!res.ok) throw new Error(res.json?.message || res.statusText)

        dispatch(fetchGuildsResolved(res.json))

        if (res.json.length) {
            const selectedGuild = res.json[0]
            dispatch(selectGuild(selectedGuild.id))
            dispatch(fetchSounds(selectedGuild.id))
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

        if (!res.ok) throw new Error(res.json?.message || res.statusText)

        dispatch(fetchGuildMembersResolved(guildId, res.json))
    } catch (error) {
        message.error(`Failed to get guild members: ${error.message}`)
        dispatch(fetchGuildMembersRejected(error))
    }
}

export const selectGuild = (guildId: string): SelectGuildAction => ({
    type: SELECT_GUILD,
    payload: guildId
})