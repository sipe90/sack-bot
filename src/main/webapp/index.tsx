import React from 'react'
import ReactDOM from 'react-dom'
import { Provider } from 'react-redux'
import { applyMiddleware, createStore, Middleware } from 'redux'
import { composeWithDevTools } from 'redux-devtools-extension'
import { createLogger } from 'redux-logger'
import thunkMiddleware from 'redux-thunk'
import { Spin, message } from 'antd'
import { LoadingOutlined } from '@ant-design/icons'

import reducers from '@/reducers'
import App from '@/components/App'

message.config({
    duration: 3,
    maxCount: 3,
    top: 72,
})

Spin.setDefaultIndicator(<LoadingOutlined />)

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
        <App />
    </Provider>,
    document.getElementById('root'),
)
