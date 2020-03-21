import { IMember, IGuild, AsyncThunkResult } from "@/types"
import { fetchGetJson } from "@/util"
import { fetchSounds } from "./sounds"

export const FETCH_USER_REQUEST = "FETCH_USER_REQUEST"
export const FETCH_USER_RESOLVED = "FETCH_USER_RESOLVED"
export const FETCH_USER_REJECTED = "FETCH_USER_REJECTED"

export const FETCH_GUILDS_REQUEST = "FETCH_GUILDS_REQUEST"
export const FETCH_GUILDS_RESOLVED = "FETCH_GUILDS_RESOLVED"
export const FETCH_GUILDS_REJECTED = "FETCH_GUILDS_REJECTED"

export const SELECT_GUILD = "SELECT_GUILD"

interface FetchUserRequestAction {
    type: typeof FETCH_USER_REQUEST
}

interface FetchUserResolvedAction {
    type: typeof FETCH_USER_RESOLVED,
    payload: IMember[]
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

interface SelectGuildAction {
    type: typeof SELECT_GUILD,
    payload: string
}

export type UserActions = FetchUserRequestAction | FetchUserResolvedAction | FetchUserRejectedAction | 
    FetchGuildsRequestAction | FetchGuildsResolvedAction | FetchGuildsRejectedAction |
    SelectGuildAction

const fetchUserRequest = (): FetchUserRequestAction => ({
    type: FETCH_USER_REQUEST
})

const fetchUserResolved = (memberships: IMember[]): FetchUserResolvedAction => ({
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
        const res = await fetchGetJson<IMember[]>('/api/me')

        if (!res.ok) throw new Error(res.json?.message || res.statusText)

        dispatch(fetchUserResolved(res.json))
    } catch (error) {
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
        dispatch(fetchGuildsRejected(error))
    }
}

export const selectGuild = (guildId: string): SelectGuildAction => ({
    type: SELECT_GUILD,
    payload: guildId
})