import { message } from "antd"

import { ActionGroup, ISettings, AsyncThunk } from "@/types"
import { fetchGetJson, apiThunk } from "@/util"

export const FETCH_SETTINGS_REQUEST = "FETCH_SETTINGS_REQUEST"
export const FETCH_SETTINGS_RESOLVED = "FETCH_SETTINGS_RESOLVED"
export const FETCH_SETTINGS_REJECTED = "FETCH_SETTINGS_REJECTED"

export type SettingsActions = ActionGroup<typeof FETCH_SETTINGS_REQUEST, typeof FETCH_SETTINGS_RESOLVED, typeof FETCH_SETTINGS_REJECTED, ISettings>

export const fetchSettings = (): AsyncThunk => async (dispatch, getState) => {
    dispatch(apiThunk({
        types: [FETCH_SETTINGS_REQUEST, FETCH_SETTINGS_RESOLVED, FETCH_SETTINGS_REJECTED],
        apiCall: () => fetchGetJson<ISettings>(`/api/settings`),
        onError: (err) => getState().user.loggedIn && message.error(`Failed to fetch settings: ${err.message}`),
    }))
}
