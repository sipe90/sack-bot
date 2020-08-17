import * as RR from 'react-redux'

import { AppDispatch } from '@/types'
import { IAppState } from '@/reducers'


export const useSelector: RR.TypedUseSelectorHook<IAppState> = RR.useSelector
export const useDispatch = () => RR.useDispatch<AppDispatch>()


export type JsonResponse<E = void> = ({
    headers: Headers
    status: number
    statusText: string
} & ({
    ok: true
    json: E
} | {
    ok: false
    json: IErrorResponse | null
})
)

interface IErrorResponse {
    field: string
    message: string
    stack?: string
}

const isJsonResponse = (headers: Headers) => !!headers.get('content-type')?.includes('application/json')

const fetchJson = async <E>(url: string, init?: RequestInit): Promise<JsonResponse<E>> => {
    const res = await fetch(url, init)
    const { headers, json, ok, status, statusText } = res

    const resJson = isJsonResponse(headers) ? await json.call(res) : null

    return ok ? {
        headers,
        json: resJson as E,
        ok,
        status,
        statusText,
    } : {
            headers,
            json: resJson as IErrorResponse,
            ok,
            status,
            statusText,
        }
}

export const fetchGetJson = <E = void>(url: string) => fetchJson<E>(url)

export const fetchPostJson = <E = void>(url: string, body?: object | string) =>
    fetchJson<E>(url, {
        body: typeof body === 'string' ? body : JSON.stringify(body),
        headers: typeof body === 'undefined' ? undefined : { 'content-type': 'application/json' },
        method: 'POST'
    })

export const fetchPutJson = <E = void>(url: string, body?: object | string) =>
    fetchJson<E>(url, {
        body: typeof body === 'string' ? body : JSON.stringify(body),
        headers: typeof body === 'undefined' ? undefined : { 'content-type': 'application/json' },
        method: 'PUT'
    })

export const fetchDeleteJson = <E = void>(url: string) => fetchJson<E>(url, { method: 'DELETE' })

export const buildQueryString = (params: { [key: string]: any | any[] }) =>
    Object
        .keys(params)
        .filter(key => Array.isArray(params[key]) ? params[key].length : params[key] !== undefined)
        .map(key => Array.isArray(params[key]) ?
            params[key].map((val: any) => `${encodeURIComponent(key)}=${encodeURIComponent(val)}`).join('&') :
            `${encodeURIComponent(key)}=${encodeURIComponent(params[key])}`
        ).join('&')

type Types = [string, string, string]
type ApiCall<T> = () => Promise<JsonResponse<T>>
type ResponseMapper<T, P> = (res: T) => P
type ErrorResponseHandler<T> = (res: JsonResponse<T>) => void

interface IThunkOpts<T, P> {
    types: Types
    apiCall: ApiCall<T>
    onError?: ErrorResponseHandler<T>
    responseMapper?: ResponseMapper<T, P>
}

export const apiThunk = <T = void, P = void>(opts: IThunkOpts<T, P>) => async (dispatch: AppDispatch) => {
    const { types, apiCall, responseMapper, onError } = opts
    const [requestType, resolvedType, rejectedType] = types

    dispatch({ type: requestType })
    const res = await apiCall()

    if (res.ok) {
        dispatch({ type: resolvedType, payload: responseMapper ? responseMapper(res.json) : res.json })
        return res.json
    } else {
        dispatch({ type: rejectedType, payload: new Error(res.json?.message || res.statusText) })
        onError && onError(res)
    }
}