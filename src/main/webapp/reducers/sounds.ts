import { IAudioFile } from "@/types";
import { Reducer } from "redux";
import { FETCH_SOUNDS_REQUEST, FETCH_SOUNDS_RESOLVED, FETCH_SOUNDS_REJECTED,
    PLAY_SOUND_REQUEST, PLAY_SOUND_RESOLVED, PLAY_SOUND_REJECTED,
    PLAY_RANDOM_SOUND_REQUEST, PLAY_RANDOM_SOUND_RESOLVED, PLAY_RANDOM_SOUND_REJECTED,
    SoundsActions,
    DELETE_SOUND_REQUEST, DELETE_SOUND_RESOLVED, DELETE_SOUND_REJECTED,
    UPDATE_SOUND_REQUEST, UPDATE_SOUND_RESOLVED, UPDATE_SOUND_REJECTED,
    PLAY_URL_REQUEST, PLAY_URL_RESOLVED, PLAY_URL_REJECTED 
} from "@/actions/sounds";

export interface ISoundsState {
    sounds: IAudioFile[]
    soundsLoading: boolean
    playingSound: boolean
}

const initialState: ISoundsState = {
    sounds: [],
    soundsLoading: false,
    playingSound: false
}

const soundsReducer: Reducer<ISoundsState, SoundsActions> = (state = initialState, action) => {
    switch(action.type) {
        case FETCH_SOUNDS_REQUEST:
            return { ...state, soundsLoading: true }
        case FETCH_SOUNDS_RESOLVED:
            return { 
                ...state, 
                soundsLoading: false,
                sounds: action.payload
            }
        case FETCH_SOUNDS_REJECTED:
            return { ...state, soundsLoading: false }
        case PLAY_SOUND_REQUEST:
            return { ...state, playingSound: true }
        case PLAY_SOUND_RESOLVED:
            return { ...state, playingSound: false }
        case PLAY_SOUND_REJECTED:
            return { ...state, playingSound: false }
        case PLAY_RANDOM_SOUND_REQUEST:
            return { ...state, playingSound: true }
        case PLAY_RANDOM_SOUND_RESOLVED:
            return { ...state, playingSound: false }
        case PLAY_RANDOM_SOUND_REJECTED:
            return { ...state, playingSound: false }
        case PLAY_URL_REQUEST:
            return { ...state, playingSound: true }
        case PLAY_URL_RESOLVED:
            return { ...state, playingSound: false }
        case PLAY_URL_REJECTED:
            return { ...state, playingSound: false }
        case UPDATE_SOUND_REQUEST:
            return { ...state }
        case UPDATE_SOUND_RESOLVED:
            return { ...state }
        case UPDATE_SOUND_REJECTED:
            return { ...state }
        case DELETE_SOUND_REQUEST:
            return { ...state }
        case DELETE_SOUND_RESOLVED:
            return { ...state }
        case DELETE_SOUND_REJECTED:
            return { ...state }
        default:
            return state
    }
}

export default soundsReducer