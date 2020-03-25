import { IVoiceLines } from "@/types";
import { Reducer } from "redux";
import { FETCH_VOICE_LINES_REQUEST, FETCH_VOICE_LINES_RESOLVED, FETCH_VOICE_LINES_REJECTED, PLAY_VOICE_LINES_REQUEST, PLAY_VOICE_LINES_RESOLVED, PLAY_VOICE_LINES_REJECTED, VoicesActions } from "@/actions/voices";

export interface IVoicesState {
    voiceLines: IVoiceLines
    voiceLinesLoading: boolean
    playingVoiceLines: boolean
}

const initialState: IVoicesState = {
    voiceLines: {},
    voiceLinesLoading: false,
    playingVoiceLines: false
}

const voicesReducer: Reducer<IVoicesState, VoicesActions> = (state = initialState, action) => {
    switch(action.type) {
        case FETCH_VOICE_LINES_REQUEST:
            return { ...state, voiceLinesLoading: true }
        case FETCH_VOICE_LINES_RESOLVED:
            return { 
                ...state, 
                voiceLinesLoading: false,
                voiceLines: action.payload
            }
        case FETCH_VOICE_LINES_REJECTED:
            return { ...state, voiceLinesLoading: false }
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