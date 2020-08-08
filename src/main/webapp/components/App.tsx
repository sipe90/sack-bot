import React, { useEffect } from 'react'
import { Layout } from 'antd'
import styled from 'styled-components'
import { Router, Switch, Route, Redirect } from 'react-router-dom'

import { useDispatch, fetchGetJson, useSelector } from '@/util'
import history from '@/history'
import { fetchUser, fetchGuilds } from '@/actions/user'
import { selectedGuild } from '@/selectors/user'
import Navigation from '@/components/Navigation'
import Soundboard from '@/components/Soundboard'
import Voices from '@/components/Voices'
import TTS from '@/components/TTS'
import Admin from '@/components/Admin'
import NotFound from '@/components/NotFound'
import Login from '@/components/Login'

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
const ContentWrapper = styled(Content)`
    padding: 6px;
`

const AppFooter = styled(Footer)`
    background-color: rgb(255, 255, 255);
    text-align: center;
`

const App: React.FC = () => (
    <Root>
        <Router history={history}>
            <AppLayout>
                <Switch>
                    <Route path='/login' exact>
                        <Login />
                    </Route>
                    <Route>
                        <AppHeader>
                            <Navigation />
                        </AppHeader>
                        <AppContent />
                    </Route>
                </Switch>
                <AppFooter>Sackbot {VERSION ? `v${VERSION}` : ''}</AppFooter>
            </AppLayout>
        </Router>
    </Root>
)

const AppContent: React.FC = () => {
    const dispatch = useDispatch()

    const guild = useSelector(selectedGuild)

    const isAdmin = guild !== null && guild.isAdmin

    useEffect(() => {
        dispatch(fetchUser())
        dispatch(fetchGuilds())

        setInterval(() => fetchGetJson('api/ping'), 5 * 60 * 1000)
    }, [])

    return (
        <ContentWrapper>
            <Switch>
                <Redirect exact from='/' to='/board' />
                <Route path='/board' exact>
                    <Soundboard />
                </Route>
                <Route path='/voices' exact>
                    <Voices />
                </Route>
                <Route path='/tts' exact>
                    <TTS />
                </Route>
                <Route path='/admin' exact>
                    {isAdmin ? <Admin /> : <NotFound />}
                </Route>
                <NotFound />
            </Switch>
        </ContentWrapper>
    )
}

export default App