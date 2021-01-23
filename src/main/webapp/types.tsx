import { Action } from "redux"
import { ThunkDispatch, ThunkAction } from "redux-thunk"
import { IAppState } from "@/reducers"
import { OptionsObject, SnackbarKey } from 'notistack'

export interface IDictionary<A> {
    [index: string]: A
}

export type AppAction = Action<string>
export type AppReducer<S, A extends AppAction> = (state: S, action: A) => S
export type AppDispatch = ThunkDispatch<IAppState, undefined, AppAction>

export type Thunk<R = void, A extends Action = AppAction> = ThunkAction<R, IAppState, unknown, A>
export type AsyncThunk<R = void, A extends Action = AppAction> = Thunk<Promise<R>, A>

export type ActionGroup<T1, T2, T3, P = undefined> = RequestAction<T1> | ResolvedAction<T2, P> | RejectedAction<T3>

export interface RequestAction<T> {
    type: T
}

export interface ResolvedAction<T, P = undefined> {
    type: T
    payload: P
}

export interface RejectedAction<T> {
    type: T
    payload: Error
}

export interface Notification {
    key: SnackbarKey
    message: React.ReactNode
    options?: OptionsObject
    dismissed?: boolean
}

export interface UserInfo {
    name: string
    avatarUrl?: string
    memberships: IMembership[]
}

export interface IMembership {
    guildId: string
    userId: string
    entrySound: string | null
    exitSound: string | null
    createdBy: string
    created: number
    modifiedBy: string | null
    modified: number | null
}

export interface IGuild {
    id: string,
    name: string,
    iconUrl: string | null,
    isAdmin: boolean,
    roles: string[]
}

export interface IGuildMember {
    guildId: string
    id: string
    name: string
}

export interface IAudioFile {
    name: string
    extension?: string
    size: number
    guildId: string
    tags: string[]
    createdBy: string
    created: number
    modifiedBy: string | null
    modified: number | null
}

export interface IVoiceLines {
    [voice: string]: string[]
}

export interface ISettings {
    upload: {
        sizeLimit: number
        overrideExisting: boolean
    }
}