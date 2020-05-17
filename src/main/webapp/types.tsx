import { Action, AnyAction } from "redux"
import { ThunkDispatch, ThunkAction } from "redux-thunk"
import { IAppState } from "@/reducers"

export interface IDictionary<A> {
    [index: string]: A;
}

export type AppAction = Action<string>
export type AppReducer<S, A extends AppAction> = (state: S, action: A) => S
export type AppDispatch = ThunkDispatch<IAppState, undefined, AnyAction>


export type ThunkResult<R = void, A extends Action = AnyAction> = ThunkAction<R, IAppState, undefined, A>
export type AsyncThunkResult<R = void, A extends Action = AnyAction> = ThunkResult<Promise<R>, A>

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