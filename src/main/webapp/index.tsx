import React from 'react'
import ReactDOM from 'react-dom'
import { Provider } from 'react-redux'
import { applyMiddleware, createStore } from 'redux'
import { composeWithDevTools   } from 'redux-devtools-extension';
import { createLogger } from 'redux-logger'
import thunkMiddleware from 'redux-thunk'

import reducers from '@/reducers'
import AppContainer from '@/components/AppContainer'

const loggerMiddleware = createLogger()

const store = createStore(
    reducers,
    composeWithDevTools (
    applyMiddleware(
        thunkMiddleware,
        loggerMiddleware,
    ))
)

ReactDOM.render(
    <Provider store={store}>
        <AppContainer/>
    </Provider>,
    document.getElementById('root'),
)
