import { combineReducers } from 'redux'

import user, { IUserState } from '@/reducers/user'
import sounds, { ISoundsState } from '@/reducers/sounds'
import voices, { IVoicesState } from '@/reducers/voices'
import tts, { ITTSState } from '@/reducers/tts'

export interface IAppState {
  user: IUserState
  sounds: ISoundsState
  voices: IVoicesState
  tts: ITTSState
}

export default combineReducers<IAppState>({
    user,
    sounds,
    voices,
    tts
})