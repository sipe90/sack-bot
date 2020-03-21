import React, { useEffect } from 'react'
import { Layout } from 'antd'
import styled from 'styled-components'
import { BrowserRouter as Router } from 'react-router-dom'

import { useDispatch } from '@/util'
import { fetchUser, fetchGuilds } from '@/actions/user'
import Navigation from '@/components/Navigation'
import Routes from '@/components/Routes'
import routeDefs from '@/routeDefs'

const { Header, Content, Footer } = Layout

// From Webpack define plugin
declare var VERSION: string | void

const Root = styled.div`
    background-color: #5dabcf;
`
const AppLayout = styled(Layout)`
    background-color: rgb(255, 255, 255);
    box-shadow: 0 4px 8px 0 rgba(0, 0, 0, 0.2), 0 6px 20px 0 rgba(0, 0, 0, 0.19);
    min-height: 100vh;
    max-width: 1080px;
    margin: auto;
`

const AppHeader = styled(Header)`
    height: 52px;
    padding: 0;
    line-height: normal;
`

const AppContent = styled(Content)`

`

const AppFooter = styled(Footer)`
    background-color: rgb(255, 255, 255);
    text-align: center;
`

const App: React.FC = () => {

    const dispatch = useDispatch()

    useEffect(() => {
        dispatch(fetchUser())
        dispatch(fetchGuilds())
    }, [])

    return (
        <Root>
            <Router>
                <AppLayout>
                    <AppHeader>
                        <Navigation routes={routeDefs} />
                    </AppHeader>
                    <AppContent>
                        <Routes routes={routeDefs} />
                    </AppContent>
                    <AppFooter>Sackbot {VERSION ? `v${VERSION}` : ''}</AppFooter>
                </AppLayout>
            </Router>
        </Root>
    )
}

export default App