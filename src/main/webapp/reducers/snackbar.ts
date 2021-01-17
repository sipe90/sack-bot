import { Reducer } from 'redux'

import { ENQUEUE_SNACKBAR, CLOSE_SNACKBAR, REMOVE_SNACKBAR, SnackbarActions } from '@/actions/snackbar'
import { Notification } from '@/types'

export interface SnackbarState {
    notifications: Notification[]
}

const initialState = {
    notifications: [],
}

const snackbarReducer: Reducer<SnackbarState, SnackbarActions> = (state = initialState, action) => {
    switch (action.type) {
        case ENQUEUE_SNACKBAR:
            return {
                ...state,
                notifications: [
                    ...state.notifications,
                    action.notification
                ],
            }
        case CLOSE_SNACKBAR:
            return {
                ...state,
                notifications: state.notifications.map((notification) => (
                    (action.dismissAll || notification.key === action.key)
                        ? { ...notification, dismissed: true }
                        : notification
                )),
            }
        case REMOVE_SNACKBAR:
            return {
                ...state,
                notifications: state.notifications.filter(({ key }) => key !== action.key),
            }
        default:
            return state
    }
}

export default snackbarReducer
