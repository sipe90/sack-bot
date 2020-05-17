import { createSelector } from 'reselect'
import { IAppState } from '@/reducers'
import { IMembership } from '@/types'

export const selectedGuildMembership = createSelector<IAppState, string | null, IMembership[], IMembership | null>(
    (state) => state.user.selectedGuild,
    (state) => state.user.memberships,
    (guild, memberships) => guild === null ? null :
        memberships.find((m) => m.guildId == guild) || null
)