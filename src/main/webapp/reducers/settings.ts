import { Reducer } from "redux"
import {
    FETCH_SETTINGS_REQUEST, FETCH_SETTINGS_RESOLVED, FETCH_SETTINGS_REJECTED,
    SettingsActions
} from "@/actions/settings"
import { ISettings } from '@/types'

export interface ISettingsState {
    settingsLoading: boolean
    settings: ISettings
}

const initialState: ISettingsState = {
    settingsLoading: false,
    settings: {
        upload: {
            sizeLimit: 0,
            overrideExisting: false
        }
    }
}

const settingsReducer: Reducer<ISettingsState, SettingsActions> = (state = initialState, action) => {
    switch (action.type) {
        case FETCH_SETTINGS_REQUEST:
            return { ...state, settingsLoading: true }
        case FETCH_SETTINGS_RESOLVED:
            return { ...state, settingsLoading: false, settings: action.payload }
        case FETCH_SETTINGS_REJECTED:
            return { ...state, settingsLoading: false }
        default:
            return state
    }
}

export default settingsReducer