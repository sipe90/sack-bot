import { createSelector } from 'reselect'
import { IAppState } from '@/reducers'

export const selectedGuild = createSelector(
    (state: IAppState) => state.user.selectedGuildId,
    (state: IAppState) => state.user.guilds,
    (guildId, guilds) => guildId === null ? null :
        guilds.find((g) => g.id === guildId) || null
)

export const selectedGuildMembership = createSelector(
    (state: IAppState) => state.user.selectedGuildId,
    (state: IAppState) => state.user.userInfo?.memberships || [],
    (guild, memberships) => guild === null ? null :
        memberships.find((m) => m.guildId == guild) || null
)

export const selectedGuildMembers = createSelector(
    (state: IAppState) => state.user.selectedGuildId,
    (state: IAppState) => state.user.guildMembers,
    (guild, members) => guild === null ? null : members[guild]
)