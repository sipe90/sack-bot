import { combineReducers } from 'redux'

import user, { IUserState } from '@/reducers/user';
import sounds, { ISoundsState } from '@/reducers/sounds';

export interface IAppState {
  user: IUserState
  sounds: ISoundsState
}

export default combineReducers<IAppState>({
    user,
    sounds
})