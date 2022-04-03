import React from 'react'
import ReactDOM from 'react-dom'
import { Provider } from 'react-redux'
import { BrowserRouter } from 'react-router-dom'
import { applyMiddleware, createStore, Middleware } from 'redux'
import { composeWithDevTools } from 'redux-devtools-extension'
import { createLogger } from 'redux-logger'
import thunkMiddleware from 'redux-thunk'
import { SnackbarProvider } from 'notistack'

import reducers from '@/reducers'
import App from '@/components/App'

const middlewares: Middleware[] = [thunkMiddleware]

if (process.env.NODE_ENV === `development`) {
    middlewares.push(createLogger())
}

const store = createStore(
    reducers,
    composeWithDevTools(
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
