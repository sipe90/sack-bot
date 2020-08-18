import { createSelector } from 'reselect'
import { IAppState } from '@/reducers'
import { IMembership, IGuild, IDictionary, IGuildMember } from '@/types'

export const selectedGuild = createSelector<IAppState, string | null, IGuild[], IGuild | null>(
    (state) => state.user.selectedGuildId,
    (state) => state.user.guilds,
    (guildId, guilds) => guildId === null ? null :
        guilds.find((g) => g.id === guildId) || null
)

export const selectedGuildMembership = createSelector<IAppState, string | null, IMembership[], IMembership | null>(
    (state) => state.user.selectedGuildId,
    (state) => state.user.memberships,
    (guild, memberships) => guild === null ? null :
        memberships.find((m) => m.guildId == guild) || null
)

export const selectedGuildMembers = createSelector<IAppState, string | null, IDictionary<IGuildMember[]>, IGuildMember[] | null>(
    (state) => state.user.selectedGuildId,
    (state) => state.user.guildMembers,
    (guild, members) => guild === null ? null : members[guild]
)