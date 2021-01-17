import { ActionGroup, ISettings, AsyncThunk } from "@/types"
import { fetchGetJson, apiThunk } from "@/util"
import { enqueueSnackbar } from './snackbar'

export const FETCH_SETTINGS_REQUEST = "FETCH_SETTINGS_REQUEST"
export const FETCH_SETTINGS_RESOLVED = "FETCH_SETTINGS_RESOLVED"
export const FETCH_SETTINGS_REJECTED = "FETCH_SETTINGS_REJECTED"

export type SettingsActions = ActionGroup<typeof FETCH_SETTINGS_REQUEST, typeof FETCH_SETTINGS_RESOLVED, typeof FETCH_SETTINGS_REJECTED, ISettings>

export const fetchSettings = (): AsyncThunk => async (dispatch) => {
    dispatch(apiThunk({
        types: [FETCH_SETTINGS_REQUEST, FETCH_SETTINGS_RESOLVED, FETCH_SETTINGS_REJECTED],
        apiCall: () => fetchGetJson<ISettings>('/api/settings'),
        onError: (err, dispatch) => dispatch(enqueueSnackbar(`Failed to load settings: ${err.message}`, { variant: 'error' }))
    }))
}
