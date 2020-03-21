import React from 'react'
import ReactDOM from 'react-dom'
import { Provider } from 'react-redux'
import { applyMiddleware, createStore, Middleware } from 'redux'
import { composeWithDevTools   } from 'redux-devtools-extension';
import { createLogger } from 'redux-logger'
import thunkMiddleware from 'redux-thunk'

import reducers from '@/reducers'
import App from '@/components/App'


const middlewares: Middleware[] = [thunkMiddleware];

if (process.env.NODE_ENV === `development`) {
  middlewares.push(createLogger())
}

const store = createStore(
    reducers,
    composeWithDevTools (
    applyMiddleware(...middlewares))
)

ReactDOM.render(
    <Provider store={store}>
        <App/>
    </Provider>,
    document.getElementById('root'),
)
