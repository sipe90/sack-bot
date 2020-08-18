import { Reducer } from "redux"
import {
    TTSActions, PLAY_TTS_REQUEST, PLAY_TTS_RESOLVED, PLAY_TTS_REJECTED,
    PLAY_RANDOM_TTS_REQUEST, PLAY_RANDOM_TTS_RESOLVED, PLAY_RANDOM_TTS_REJECTED
} from "@/actions/tts"

export interface ITTSState {
    playingTTS: boolean
}

const initialState: ITTSState = {
    playingTTS: false
}

const voicesReducer: Reducer<ITTSState, TTSActions> = (state = initialState, action) => {
    switch (action.type) {
        case PLAY_TTS_REQUEST:
            return { ...state, playingTTS: true }
        case PLAY_TTS_RESOLVED:
            return { ...state, playingTTS: false }
        case PLAY_TTS_REJECTED:
            return { ...state, playingTTS: false }
        case PLAY_RANDOM_TTS_REQUEST:
            return { ...state, playingTTS: true }
        case PLAY_RANDOM_TTS_RESOLVED:
            return { ...state, playingTTS: false }
        case PLAY_RANDOM_TTS_REJECTED:
            return { ...state, playingTTS: false }
        default:
            return state
    }
}

export default voicesReducer