import { combineReducers } from 'redux'

import user, { IUserState } from '@/reducers/user'
import sounds, { ISoundsState } from '@/reducers/sounds'
import snackbar, { SnackbarState } from '@/reducers/snackbar'
import settings, { ISettingsState } from '@/reducers/settings'

export interface IAppState {
  user: IUserState
  sounds: ISoundsState
  snackbar: SnackbarState
  settings: ISettingsState
}

export default combineReducers<IAppState>({
  user,
  sounds,
  snackbar,
  settings
})