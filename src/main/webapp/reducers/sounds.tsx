import { IAudioFile } from "@/types";
import { Reducer } from "redux";
import { FETCH_SOUNDS_REQUEST, FETCH_SOUNDS_RESOLVED, FETCH_SOUNDS_REJECTED, PLAY_SOUND_REQUEST, PLAY_SOUND_RESOLVED, PLAY_SOUND_REJECTED, PLAY_RANDOM_SOUND_REQUEST, PLAY_RANDOM_SOUND_RESOLVED, PLAY_RANDOM_SOUND_REJECTED, SoundsActions } from "@/actions/sounds";

export interface ISoundsState {
    sounds: IAudioFile[]
    soundLoading: boolean
    playingSound: boolean
}

const initialState: ISoundsState = {
    sounds: [],
    soundLoading: false,
    playingSound: false
}

const userReducer: Reducer<ISoundsState, SoundsActions> = (state = initialState, action) => {
    switch(action.type) {
        case FETCH_SOUNDS_REQUEST:
            return { ...state, soundLoading: true }
        case FETCH_SOUNDS_RESOLVED:
            return { 
                ...state, 
                soundLoading: false,
                sounds: action.payload
            }
        case FETCH_SOUNDS_REJECTED:
            return { ...state, soundLoading: false }
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
        default:
            return state
    }
}

export default userReducer