import { Reducer } from "redux"
import {
    PLAY_VOICE_LINES_REQUEST, PLAY_VOICE_LINES_RESOLVED, PLAY_VOICE_LINES_REJECTED,
    VoicesActions
} from "@/actions/voices"

export interface IVoicesState {
    playingVoiceLines: boolean
}

const initialState: IVoicesState = {
    playingVoiceLines: false
}

const voicesReducer: Reducer<IVoicesState, VoicesActions> = (state = initialState, action) => {
    switch (action.type) {
        case PLAY_VOICE_LINES_REQUEST:
            return { ...state, playingVoiceLines: true }
        case PLAY_VOICE_LINES_RESOLVED:
            return { ...state, playingVoiceLines: false }
        case PLAY_VOICE_LINES_REJECTED:
            return { ...state, playingVoiceLines: false }
        default:
            return state
    }
}

export default voicesReducer