import {IGuild, IGuildMember, IMembership} from "@/types";
import {Reducer} from "redux";
import {
    FETCH_GUILD_MEMBERS_REJECTED,
    FETCH_GUILD_MEMBERS_REQUEST,
    FETCH_GUILD_MEMBERS_RESOLVED,
    FETCH_GUILDS_REJECTED,
    FETCH_GUILDS_REQUEST,
    FETCH_GUILDS_RESOLVED,
    FETCH_USER_REJECTED,
    FETCH_USER_REQUEST,
    FETCH_USER_RESOLVED,
    SELECT_GUILD,
    UPDATE_ENTRY_SOUND_REJECTED,
    UPDATE_ENTRY_SOUND_REQUEST,
    UPDATE_ENTRY_SOUND_RESOLVED,
    UPDATE_EXIT_SOUND_REJECTED,
    UPDATE_EXIT_SOUND_REQUEST,
    UPDATE_EXIT_SOUND_RESOLVED,
    UserActions
} from "@/actions/user";

export interface IUserState {
    memberships: IMembership[]
    membershipsLoading: boolean
    guilds: IGuild[]
    guildsLoading: boolean
    guildMembers: { [guildId: string]: IGuildMember[] }
    guildMembersLoading: boolean
    selectedGuild: string | null
}

const initialState: IUserState = {
    memberships: [],
    membershipsLoading: false,
    guilds: [],
    guildsLoading: false,
    guildMembers: {},
    guildMembersLoading: false,
    selectedGuild: null
}

const userReducer: Reducer<IUserState, UserActions> = (state = initialState, action) => {
    switch(action.type) {
        case FETCH_USER_REQUEST:
            return { ...state, membershipsLoading: true }
        case FETCH_USER_RESOLVED:
            return { 
                ...state, 
                membershipsLoading: false,
                memberships: action.payload
            }
        case FETCH_USER_REJECTED:
            return { ...state, membershipsLoading: false }
        case FETCH_GUILDS_REQUEST:
            return { ...state, guildsLoading: true }
        case FETCH_GUILDS_RESOLVED:
            return { 
                ...state, 
                guildsLoading: false,
                guilds: action.payload
            }
        case FETCH_GUILDS_REJECTED:
            return { ...state, guildsLoading: false }
        case FETCH_GUILD_MEMBERS_REQUEST:
            return { ...state, guildMembersLoading: true }
        case FETCH_GUILD_MEMBERS_RESOLVED:
            return { 
                ...state, 
                guildMembersLoading: false,
                guildMembers: { ...state.guildMembers, [action.payload.guildId]: action.payload.members }
            }
        case FETCH_GUILD_MEMBERS_REJECTED:
            return { ...state, guildMembersLoading: false }
        case UPDATE_ENTRY_SOUND_REQUEST:
            return { ...state }
        case UPDATE_ENTRY_SOUND_RESOLVED:
            return { ...state }
        case UPDATE_ENTRY_SOUND_REJECTED:
            return { ...state }
        case UPDATE_EXIT_SOUND_REQUEST:
            return { ...state }
        case UPDATE_EXIT_SOUND_RESOLVED:
            return { ...state }
        case UPDATE_EXIT_SOUND_REJECTED:
            return { ...state }
        case SELECT_GUILD:
            return { ...state, selectedGuild: action.payload }
        default:
            return state
    }
}

export default userReducer