import React, { useEffect } from 'react'
import { useDispatch, useSelector } from 'react-redux'
import { SnackbarKey, useSnackbar } from 'notistack'

import { Notification } from '@/types'
import { removeSnackbar } from '@/actions/snackbar'
import { IAppState } from '@/reducers'

let displayed: SnackbarKey[] = []

const Notifier: React.FC = () => {
    const dispatch = useDispatch()
    const notifications = useSelector<IAppState, Notification[]>(store => store.snackbar.notifications)
    const { enqueueSnackbar, closeSnackbar } = useSnackbar()

    const storeDisplayed = (id: SnackbarKey) => {
        displayed = [...displayed, id]
    }

    const removeDisplayed = (id: SnackbarKey) => {
        displayed = [...displayed.filter(key => id !== key)]
    }

    useEffect(() => {
        notifications.forEach(({ key, message, options = {}, dismissed = false }) => {
            if (dismissed) {
                closeSnackbar(key)
                return
            }

            if (displayed.includes(key)) return

            enqueueSnackbar(message, {
                key,
                ...options,
                onClose: (event, reason, myKey) => {
                    if (options.onClose) {
                        options.onClose(event, reason, myKey)
                    }
                },
                onExited: (_event, myKey) => {
                    dispatch(removeSnackbar(myKey))
                    removeDisplayed(myKey)
                },
            })

            storeDisplayed(key)
        })
    }, [notifications, closeSnackbar, enqueueSnackbar, dispatch])

    return null
}

export default Notifier