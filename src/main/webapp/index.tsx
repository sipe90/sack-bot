import React from 'react'
import ReactDOM from 'react-dom'
import { Provider } from 'react-redux'
import { applyMiddleware, createStore } from 'redux'
import { composeWithDevTools   } from 'redux-devtools-extension';
import { createLogger } from 'redux-logger'
import thunkMiddleware from 'redux-thunk'

const loggerMiddleware = createLogger()

const reducers = () => null

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
        <h1>Yo</h1>
    </Provider>,
    document.getElementById('root'),
)
