import React from 'react'
import ReactDOM from 'react-dom'
import { Provider } from 'react-redux'
import { BrowserRouter } from 'react-router-dom'
import { applyMiddleware, compose, createStore, Middleware } from 'redux'
import { createLogger } from 'redux-logger'
import thunkMiddleware from 'redux-thunk'
import { SnackbarProvider } from 'notistack'

import reducers from '@/reducers'
import App from '@/components/App'

const middlewares: Middleware[] = [thunkMiddleware]

if (process.env.NODE_ENV === `development`) {
    middlewares.push(createLogger())
}

const composeEnhancers = (window as any).__REDUX_DEVTOOLS_EXTENSION_COMPOSE__ || compose

const store = createStore(
    reducers,
    composeEnhancers(
        applyMiddleware(...middlewares))
)

ReactDOM.render(
    <Provider store={store}>
        <BrowserRouter>
            <SnackbarProvider
                maxSnack={5}
                anchorOrigin={{
                    vertical: 'top',
                    horizontal: 'left',
                }}
            >
                <App />
            </SnackbarProvider>
        </BrowserRouter>
    </Provider>,
    document.getElementById('root'),
)
