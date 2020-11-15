import React, { useEffect } from 'react'
import { Spin } from 'antd'
import { Switch, Route, Redirect, useLocation } from 'react-router-dom'
import CssBaseline from '@material-ui/core/CssBaseline'
import AppBar from '@material-ui/core/AppBar'
import Typography from '@material-ui/core/Typography'
import Container from '@material-ui/core/Container'
import { makeStyles } from '@material-ui/core/styles'

import { useDispatch, useSelector, fetchGetJson } from '@/util'
import { selectedGuild } from '@/selectors/user'
import { fetchUser, fetchGuilds } from '@/actions/user'
import { fetchSettings } from '@/actions/settings'

import Soundboard from '@/components/Soundboard'
import Voices from '@/components/Voices'
import TTS from '@/components/TTS'
import Admin from '@/components/Admin'
import NotFound from '@/components/NotFound'
import Login from '@/components/Login'
import { Header } from '@/components/layout'

// From Webpack define plugin
declare var VERSION: string | undefined

const useStyles = makeStyles((theme) => ({
    '@global': {
        ul: {
            margin: 0,
            padding: 0,
            listStyle: 'none',
        },
    },
    appBar: {
        borderBottom: `1px solid ${theme.palette.divider}`,
    },
    layout: {
        width: 'auto',
        marginLeft: theme.spacing(2),
        marginRight: theme.spacing(2),
        [theme.breakpoints.up(1080 + theme.spacing(2) * 2)]: {
            width: 1080,
            marginLeft: 'auto',
            marginRight: 'auto',
        },
    },
    footer: {
        borderTop: `1px solid ${theme.palette.divider}`,
        marginTop: theme.spacing(8),
        paddingTop: theme.spacing(3),
        paddingBottom: theme.spacing(3),
    },
}))

const App: React.FC = () => {

    const classes = useStyles()

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
        <>
            <CssBaseline />
            <AppBar position="sticky" color="default" elevation={0} className={classes.appBar} hidden={!loggedIn}>
                <Container maxWidth="lg" disableGutters>
                    <Header />
                </Container>
            </AppBar>
            <Switch>
                <Route path='/login' exact>
                    <Login />
                </Route>
                <main className={classes.layout}>
                    <Switch>
                        {!loggedIn &&
                            <Route>
                                <Typography>
                                    <Spin tip='Loading SackBot...' />
                                </Typography>
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
                </main>
                <footer className={classes.layout}>
                    <Typography variant="body2" color="textSecondary" align="center" className={classes.footer}>
                        Sackbot {VERSION ? `v${VERSION}` : ''}
                    </Typography>
                </footer>
            </Switch>
        </>
    )
}

export default App