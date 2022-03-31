import React, { useEffect, useMemo, useState } from 'react'
import { Switch, Route, Redirect, useLocation } from 'react-router-dom'
import CssBaseline from '@mui/material/CssBaseline'
import AppBar from '@mui/material/AppBar'
import Box, { BoxProps } from '@mui/material/Box'
import Typography from '@mui/material/Typography'
import Container from '@mui/material/Container'
import CircularProgress from '@mui/material/CircularProgress'
import { createTheme, ThemeProvider } from '@mui/material/styles'
import { styled } from '@mui/system'

import { useDispatch, useSelector, fetchGetJson } from '@/util'
import { selectedGuild } from '@/selectors/user'
import { fetchUser, fetchGuilds } from '@/actions/user'
import { fetchSettings } from '@/actions/settings'

import Soundboard from '@/components/Soundboard'
import Admin from '@/components/Admin'
import NotFound from '@/components/NotFound'
import Login from '@/components/Login'
import { Header } from '@/components/layout'
import Notifier from '@/components/Notifier'

// From Webpack define plugin
declare var VERSION: string | undefined

const Layout = styled(Box)<BoxProps>(({ theme }) => ({
    width: 'auto',
    marginLeft: theme.spacing(2),
    marginRight: theme.spacing(2),
    paddingTop: theme.spacing(2),
    [theme.breakpoints.up(1200)]: {
        width: 1200,
        marginLeft: 'auto',
        marginRight: 'auto',
    }
}))

const App: React.FC = () => {
    const [darkMode, setDarkMode] = useState(localStorage.getItem('darkMode') === 'true')

    useEffect(() => localStorage.setItem('darkMode', darkMode ? 'true' : 'false'), [darkMode])

    const theme = useMemo(
        () =>
            createTheme({
                palette: {
                    mode: darkMode ? 'dark' : 'light',
                },
            }),
        [darkMode]
    )

    const dispatch = useDispatch()

    const loggedIn = useSelector((state) => state.user.loggedIn)
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
        <ThemeProvider theme={theme}>
            <Box
                display='flex'
                flexDirection='column'
                margin={0}
                minHeight='100vh'
            >
                <CssBaseline />
                <Notifier />
                {loggedIn &&
                    <AppBar position='sticky' color='default' elevation={2}>
                        <Container maxWidth='lg' disableGutters>
                            <Header darkMode={darkMode} onDarkModeChange={setDarkMode} />
                        </Container>
                    </AppBar>
                }
                <Switch>
                    <Route path='/login' exact>
                        <Login />
                    </Route>
                    <>
                        <Layout component='main' flex={1}>
                            <Switch>
                                {!loggedIn &&
                                    <Route>
                                        <Box sx={{
                                            display: 'flex',
                                            justifyContent: 'center',
                                            alignItems: 'center',
                                            mt: 4,
                                            '& > *': {
                                                m: 1,
                                            }
                                        }}>
                                            <CircularProgress />
                                            <Typography>
                                                Loading SackBot..
                                            </Typography>
                                        </Box>
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
                        </Layout>
                        <Layout component='footer'>
                            <Typography variant='body2' color='textSecondary' align='center' sx={(theme) => ({
                                borderTop: `1px solid ${theme.palette.divider}`,
                                mt: 8,
                                py: 3
                            })}>
                                Sackbot {VERSION ? `v${VERSION}` : ''}
                            </Typography>
                        </Layout>
                    </>
                </Switch>
            </Box>
        </ThemeProvider>
    )
}

export default App