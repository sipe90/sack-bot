import React, { useEffect, useMemo, useState } from 'react'
import { Switch, Route, Redirect, useLocation } from 'react-router-dom'
import CssBaseline from '@material-ui/core/CssBaseline'
import AppBar from '@material-ui/core/AppBar'
import Typography from '@material-ui/core/Typography'
import Container from '@material-ui/core/Container'
import { createMuiTheme, makeStyles, ThemeProvider } from '@material-ui/core/styles'

import { useDispatch, useSelector, fetchGetJson } from '@/util'
import { selectedGuild } from '@/selectors/user'
import { fetchUser, fetchGuilds } from '@/actions/user'
import { fetchSettings } from '@/actions/settings'

import Soundboard from '@/components/Soundboard'
import Admin from '@/components/Admin'
import NotFound from '@/components/NotFound'
import Login from '@/components/Login'
import { Header } from '@/components/layout'
import { CircularProgress } from '@material-ui/core'
import Notifier from './Notifier'

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
    root: {
        display: 'flex',
        flexDirection: 'column',
        margin: 0,
        minHeight: '100vh'
    },
    loadingContainer: {
        display: 'flex',
        justifyContent: 'center',
        alignItems: 'center',
        marginTop: theme.spacing(4),
        '& > *': {
            margin: theme.spacing(1),
        }
    },
    layout: {
        width: 'auto',
        marginLeft: theme.spacing(2),
        marginRight: theme.spacing(2),
        paddingTop: theme.spacing(2),
        [theme.breakpoints.up(1200 + theme.spacing(2) * 2)]: {
            width: 1200,
            marginLeft: 'auto',
            marginRight: 'auto',
        },
    },
    main: {
        flex: 1,
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
    const guild = useSelector(selectedGuild)

    const isAdmin = !!guild?.isAdmin

    const location = useLocation()

    const [darkMode, setDarkMode] = useState(localStorage.getItem('darkMode') === 'true')

    useEffect(() => localStorage.setItem('darkMode', darkMode ? 'true' : 'false'), [darkMode])

    const theme = useMemo(
        () =>
            createMuiTheme({
                palette: {
                    type: darkMode ? 'dark' : 'light',
                },
            }),
        [darkMode],
    )

    useEffect(() => {
        if (location.pathname !== '/login') {
            dispatch(fetchUser())
            dispatch(fetchGuilds())
            dispatch(fetchSettings())
            setInterval(() => fetchGetJson('api/ping'), 5 * 60 * 1000)
        }
    }, [])


    return (
        <ThemeProvider theme={theme}>
            <div className={classes.root}>
                <CssBaseline />
                <Notifier />
                {loggedIn &&
                    <AppBar position="sticky" color="default" elevation={2}>
                        <Container maxWidth="lg" disableGutters>
                            <Header darkMode={darkMode} onDarkModeChange={setDarkMode} />
                        </Container>
                    </AppBar>
                }
                <Switch>
                    <Route path='/login' exact>
                        <Login />
                    </Route>
                    <>
                        <main className={`${classes.layout} ${classes.main}`}>
                            <Switch>
                                {!loggedIn &&
                                    <Route>
                                        <div className={classes.loadingContainer}>
                                            <CircularProgress />
                                            <Typography>
                                                Loading SackBot..
                                        </Typography>
                                        </div>
                                    </Route>
                                }
                                <Redirect exact from='/' to='/board' />
                                <Route path='/board' exact>
                                    <Soundboard />
                                </Route>
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
                    </>
                </Switch>
            </div>
        </ThemeProvider>
    )
}

export default App