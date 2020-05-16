import * as RR from 'react-redux'
import { AppDispatch } from "@/types"
import { IAppState } from "./reducers"


export const useSelector: RR.TypedUseSelectorHook<IAppState> = RR.useSelector
export const useDispatch = () => RR.useDispatch<AppDispatch>()


  type JsonResponse<E> = ({
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

const fetchJson = async <E> (url: string, init?: RequestInit): Promise<JsonResponse<E>> => {
    const res = await fetch(url, init)
    const { headers, json, ok, status, statusText } = res

    if (status == 401) {
        window.location.href = "/oauth2/authorization/discord"
    }

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

export const fetchGetJson = <E = any> (url: string) => fetchJson<E>(url)

export const fetchPostJson = <E = any> (url: string, body?: object | string) =>
    fetchJson<E>(url, {
        body: typeof body === 'string' ? body : JSON.stringify(body),
        headers: typeof body === 'undefined' ? undefined : { 'content-type': 'application/json' },
        method: 'POST'
    })

export const fetchDeleteJson = <E = any> (url: string) => fetchJson<E>(url, { method: 'DELETE' })

export const buildQueryString = (params: { [key: string]: any | any[]}) => 
    Object
        .keys(params)
        .filter(key => Array.isArray(params[key]) ? params[key].length : params[key] !== undefined)
        .map(key => Array.isArray(params[key]) ?
            params[key].map((val: any) => `${encodeURIComponent(key)}=${encodeURIComponent(val)}`).join('&') : 
            `${encodeURIComponent(key)}=${encodeURIComponent(params[key])}`
        ).join('&')
