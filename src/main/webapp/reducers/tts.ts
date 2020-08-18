import { Reducer } from "redux"
import {
    TTSActions, PLAY_TTS_REQUEST, PLAY_TTS_RESOLVED, PLAY_TTS_REJECTED,
    PLAY_RANDOM_TTS_REQUEST, PLAY_RANDOM_TTS_RESOLVED, PLAY_RANDOM_TTS_REJECTED,
    FETCH_VOICES_REQUEST, FETCH_VOICES_RESOLVED, FETCH_VOICES_REJECTED
} from "@/actions/tts"

export interface ITTSState {
    loadingVoices: boolean
    voices: string[]
    playingTTS: boolean
}

const initialState: ITTSState = {
    loadingVoices: false,
    voices: [],
    playingTTS: false
}

const voicesReducer: Reducer<ITTSState, TTSActions> = (state = initialState, action) => {
    switch (action.type) {
        case FETCH_VOICES_REQUEST:
            return { ...state, loadingVoices: true }
        case FETCH_VOICES_RESOLVED:
            return { ...state, loadingVoices: false, voices: action.payload }
        case FETCH_VOICES_REJECTED:
            return { ...state, loadingVoices: false }
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