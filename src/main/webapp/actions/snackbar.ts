import { OptionsObject, SnackbarKey } from 'notistack'
import { Action } from 'redux'

import { Notification } from '@/types'

export const ENQUEUE_SNACKBAR = 'ENQUEUE_SNACKBAR'
export const CLOSE_SNACKBAR = 'CLOSE_SNACKBAR'
export const REMOVE_SNACKBAR = 'REMOVE_SNACKBAR'

export type SnackbarActions = EnqueueSnackbarAction | CloseSnackbarAction | RemoveSnackbarAction

interface EnqueueSnackbarAction extends Action<typeof ENQUEUE_SNACKBAR> {
    notification: Notification
}

interface CloseSnackbarAction extends Action<typeof CLOSE_SNACKBAR> {
    dismissAll: boolean
    key: SnackbarKey
}

interface RemoveSnackbarAction extends Action<typeof REMOVE_SNACKBAR> {
    key: SnackbarKey
}

export const enqueueErrorSnackbar = (message: React.ReactNode, options?: OptionsObject) =>
    enqueueSnackbar(message, { ...options, variant: 'error' })

export const enqueueSnackbar = (message: React.ReactNode, options?: OptionsObject) => {
    const key = options?.key
    return {
        type: ENQUEUE_SNACKBAR,
        notification: {
            message,
            options,
            key: key || new Date().getTime() + Math.random(),
        },
    }
}

export const closeSnackbar = (key: SnackbarKey) => ({
    type: CLOSE_SNACKBAR,
    dismissAll: !key,
    key,
})

export const removeSnackbar = (key: SnackbarKey) => ({
    type: REMOVE_SNACKBAR,
    key,
})
