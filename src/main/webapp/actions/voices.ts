import { message } from "antd"

import { ActionGroup } from "@/types"
import { fetchPostJson, apiThunk } from "@/util"

export const PLAY_VOICE_LINES_REQUEST = "PLAY_VOICE_LINES_REQUEST"
export const PLAY_VOICE_LINES_RESOLVED = "PLAY_VOICE_LINES_RESOLVED"
export const PLAY_VOICE_LINES_REJECTED = "PLAY_VOICE_LINES_REJECTED"

export type VoicesActions = ActionGroup<typeof PLAY_VOICE_LINES_REQUEST, typeof PLAY_VOICE_LINES_RESOLVED, typeof PLAY_VOICE_LINES_REJECTED>

export const playVoiceLines = (guildId: string, voice: string, voiceLines: string[]) => apiThunk({
    types: [PLAY_VOICE_LINES_REQUEST, PLAY_VOICE_LINES_RESOLVED, PLAY_VOICE_LINES_REJECTED],
    apiCall: () => fetchPostJson(`/api/${guildId}/voices/play?voice=${voice}`, voiceLines),
    onError: (err) => message.error(`Failed to play voice lines: ${err.message}`)
})
