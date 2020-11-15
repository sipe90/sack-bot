import React, { useEffect } from 'react'
import { useLocation } from 'react-router-dom'
import queryString from 'query-string'
import { Avatar, Button, Grid, makeStyles, Paper, Typography } from '@material-ui/core'
import { useSnackbar } from 'notistack'

import sackbotImg from '@/public/img/Sackbot_V3.jpg'
import sackbotImg_alt from '@/public/img/Sackbot_V9001.png'
import sackbotAvatar from '@/public/img/Sackbot_V3_cropped.jpg'

const DiscordSvg = () => <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="2 2 46 46" fill="#fff">
    <path d="M40,12c0,0-4.585-3.588-10-4l-0.488,0.976C34.408,10.174,36.654,11.891,39,14c-4.045-2.065-8.039-4-15-4s-10.955,1.935-15,4c2.346-2.109,5.018-4.015,9.488-5.024L18,8c-5.681,0.537-10,4-10,4s-5.121,7.425-6,22c5.162,5.953,13,6,13,6l1.639-2.185C13.857,36.848,10.715,35.121,8,32c3.238,2.45,8.125,5,16,5s12.762-2.55,16-5c-2.715,3.121-5.857,4.848-8.639,5.815L33,40c0,0,7.838-0.047,13-6C45.121,19.425,40,12,40,12z M17.5,30c-1.933,0-3.5-1.791-3.5-4c0-2.209,1.567-4,3.5-4s3.5,1.791,3.5,4C21,28.209,19.433,30,17.5,30z M30.5,30c-1.933,0-3.5-1.791-3.5-4c0-2.209,1.567-4,3.5-4s3.5,1.791,3.5,4C34,28.209,32.433,30,30.5,30z"></path>
</svg>

const errorMessages: { [key: string]: string } = {
    'access_denied': 'Login failed because you are not a member of any SackBot enabled Discord guild.'
}

const backgroundImg = (Math.random() * 100) > 5 ? sackbotImg : sackbotImg_alt

const useStyles = makeStyles((theme) => ({
    root: {
        height: '100vh'
    },
    image: {
        backgroundImage: `url(${backgroundImg})`,
        backgroundRepeat: 'no-repeat',
        backgroundColor:
            theme.palette.type === 'light' ? theme.palette.grey[50] : theme.palette.grey[900],
        backgroundSize: 'cover',
        backgroundPosition: 'top',
    },
    avatar: {
        margin: theme.spacing(1),
    },
    paper: {
        margin: theme.spacing(8, 4),
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
    },
    submit: {
        margin: theme.spacing(3, 0, 2),
    },
    discordIcon: {
        fill: '#fff'
    }
}))

const Login: React.FC = () => {

    const classes = useStyles()
    const { enqueueSnackbar } = useSnackbar()

    const { search } = useLocation()

    useEffect(() => {
        const queryParams = queryString.parse(search)

        const logoutParam = queryParams['logout']
        const errorParam = queryParams['error']

        if (logoutParam !== undefined) {
            enqueueSnackbar("Logout successful.", { variant: 'success' })
        }

        if (typeof errorParam === 'string') {
            if (errorParam in errorMessages) {
                enqueueSnackbar(errorMessages[errorParam], { variant: 'error' })
            } else {
                enqueueSnackbar('An error occurred while logging in. Please try again later.', { variant: 'error' })
            }
        }
    }, [])

    return (
        <Grid component='main' container className={classes.root}>
            <Grid item xs={false} sm={4} md={7} className={classes.image} />
            <Grid item xs={12} sm={8} md={5} component={Paper} elevation={6} square>
                <div className={classes.paper}>
                    <Avatar className={classes.avatar} src={sackbotAvatar} />
                    <Typography component="h1" variant="h5">
                        Welcome to SackBot!
                    </Typography>
                    <Typography component="h2" variant="body2">
                        Please log in via Discord to use SackBot.
                    </Typography>
                    <Button
                        fullWidth
                        variant="contained"
                        color="primary"
                        href='/oauth2/authorization/discord'
                        startIcon={<DiscordSvg />}
                        className={classes.submit}
                    >
                        Sign In
                    </Button>
                </div>
            </Grid>
        </Grid>
    )
}

export default Login