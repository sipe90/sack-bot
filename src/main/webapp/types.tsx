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

export interface IMember {
    guildId: string
    userId: string
    entrySound: string | null
    exitSound: string | null
    // FIXME: Set not nullable
    createdBy: string | null
    // FIXME: Set not nullable
    created: number | null
    modifiedBy: string | null
    modified: number | null
}

export interface IGuild {
    id: string,
    name: string,
    iconUrl: string | null,
    owner: boolean,
    roles: string[]
}

export interface IAudioFile {
    name: string
    extension?: string
    size: number
    guildId: string
    createdBy: string
    created: number
    modifiedBy?: string
    modified?: number
}