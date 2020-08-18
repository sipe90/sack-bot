import React, { useEffect } from 'react'
import { Layout, Spin } from 'antd'
import styled from 'styled-components'
import { Switch, Route, Redirect, useLocation } from 'react-router-dom'

import { useDispatch, useSelector, fetchGetJson } from '@/util'
import { selectedGuild } from '@/selectors/user'
import { fetchUser, fetchGuilds } from '@/actions/user'
import { fetchSettings } from '@/actions/settings'
import { Header, Footer, Content } from '@/components/layout'
import Navigation from '@/components/Navigation'
import Soundboard from '@/components/Soundboard'
import Voices from '@/components/Voices'
import TTS from '@/components/TTS'
import Admin from '@/components/Admin'
import NotFound from '@/components/NotFound'
import Login from '@/components/Login'

// From Webpack define plugin
declare var VERSION: string | undefined

const Root = styled.div`
    background-color: rgb(247, 247, 247);
`
const AppLayout = styled(Layout)`
    box-shadow: 0 4px 8px 0 rgba(0, 0, 0, 0.02), 0 6px 20px 0 rgba(0, 0, 0, 0.12);
    min-height: 100vh;
    max-width: 1080px;
    margin: auto;
`

const Loading = styled.div`
    display: flex;
    justify-content: center;
    align-items: center;
    height: 100%;
`

const App: React.FC = () => {

    const dispatch = useDispatch()

    const loggedIn = useSelector((state) => state.user.loggedIn)
    const settings = useSelector((state) => state.settings.settings)
    const guild = useSelector(selectedGuild)

    const isAdmin = !!guild?.isAdmin

    const location = useLocation()

    useEffect(() => {
        if (location.pathname !== '/login') {
            dispatch(fetchUser())
            dispatch(fetchGuilds())
            dispatch(fetchSettings())

            setInterval(() => fetchGetJson('api/ping'), 5 * 60 * 1000)
        }
    }, [])


    return (
        <Root>
            <AppLayout>
                <Header>
                    {loggedIn && <Navigation />}
                </Header>
                <Content>
                    <Switch>
                        <Route path='/login' exact>
                            <Login />
                        </Route>
                        {!loggedIn &&
                            <Route>
                                <Loading>
                                    <Spin tip='Loading SackBot...' />
                                </Loading>
                            </Route>
                        }
                        <Redirect exact from='/' to='/board' />
                        <Route path='/board' exact>
                            <Soundboard />
                        </Route>
                        {settings.voice.enabled &&
                            <Route path='/voices' exact>
                                <Voices />
                            </Route>
                        }
                        {settings.tts.enabled &&
                            <Route path='/tts' exact>
                                <TTS />
                            </Route>
                        }
                        {isAdmin &&
                            <Route path='/admin' exact>
                                <Admin />
                            </Route>
                        }
                        <NotFound />
                    </Switch>
                </Content>
                <Footer>
                    Sackbot {VERSION ? `v${VERSION}` : ''}
                </Footer>
            </AppLayout>
        </Root>
    )
}

export default App