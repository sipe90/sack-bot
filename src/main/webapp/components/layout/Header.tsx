import React from 'react'
import { NavLink } from 'react-router-dom'

import { useSelector, useDispatch } from '@/util'
import { selectGuild } from '@/actions/user'
import { selectedGuild } from '@/selectors/user'
import { Avatar, Container, Divider, Link, ListItemAvatar, ListItemIcon, makeStyles, Menu, MenuItem, Toolbar, Typography } from '@material-ui/core'
import ExitToAppIcon from '@material-ui/icons/ExitToApp'

const useStyles = makeStyles((theme) => ({
    toolbar: {
        borderBottom: `1px solid ${theme.palette.divider}`,
    },
    toolbarTitle: {
        flexBasis: 0
    },
    toolbarSecondary: {
        justifyContent: 'center',
        overflowX: 'auto',
    },
    toolbarLink: {
        padding: theme.spacing(1),
        flexShrink: 0,
    },
    activeLink: {
        color: theme.palette.primary.main
    },
    guildSelector: {
        flexBasis: 0
    },
    menuAvatar: {
        width: theme.spacing(4),
        height: theme.spacing(4),
    },
}))

const Header: React.FC = () => {

    const classes = useStyles()

    const settings = useSelector((state) => state.settings.settings)
    const guild = useSelector(selectedGuild)

    return (<>
        <Toolbar className={classes.toolbar} disableGutters>
            <Container className={classes.toolbarTitle}>
                <Typography
                    component="h2"
                    variant="h5"
                    color="inherit"
                >
                    SackBot
                </Typography>
            </Container>
            <Container component="nav">
                <Link
                    component={NavLink}
                    color="inherit"
                    noWrap
                    variant="body2"
                    className={classes.toolbarLink}
                    to='/board'
                    exact
                    activeClassName={classes.activeLink}
                >
                    Board
                </Link>
                {settings.voice.enabled &&
                    <Link
                        component={NavLink}
                        color="inherit"
                        noWrap
                        variant="body2"
                        className={classes.toolbarLink}
                        to='/voices'
                        exact
                        activeClassName={classes.activeLink}
                    >
                        Voices
                </Link>
                }
                {settings.tts.enabled &&
                    <Link
                        component={NavLink}
                        color="inherit"
                        noWrap
                        variant="body2"
                        className={classes.toolbarLink}
                        to='/tts'
                        exact
                        activeClassName={classes.activeLink}
                    >
                        Text to Speech
                </Link>
                }
                {!!guild?.isAdmin &&
                    <Link
                        component={NavLink}
                        color="inherit"
                        noWrap
                        variant="body2"
                        className={classes.toolbarLink}
                        to='/admin'
                        exact
                        activeClassName={classes.activeLink}
                    >
                        Admin
                </Link>
                }
            </Container>
            <Container className={classes.guildSelector}>
                <GuildSelector />
            </Container>
        </Toolbar>
    </>)
}


const GuildSelector: React.FC = () => {

    const classes = useStyles()

    const guild = useSelector(selectedGuild)
    const guilds = useSelector((state) => state.user.guilds)

    const debug = guilds[0] ? [guilds[0], guilds[0], guilds[0], guilds[0], guilds[0]] : []

    const [anchorEl, setAnchorEl] = React.useState<null | HTMLElement>(null)

    const dispatch = useDispatch()

    const handleClickAvatar = (event: React.MouseEvent<HTMLElement>) => {
        setAnchorEl(event.currentTarget)
    }

    const handleClose = () => {
        setAnchorEl(null)
    }

    const handleSelectGuild = (event: React.MouseEvent<HTMLElement>) => {
        dispatch(selectGuild(event.currentTarget.id))
        handleClose()
    }

    return (
        <>
            <div onClick={handleClickAvatar}>
                <Avatar
                    alt={guild?.name}
                    src={guild?.iconUrl || undefined}
                />
            </div>
            <Menu
                anchorEl={anchorEl}
                keepMounted
                open={Boolean(anchorEl)}
                onClose={handleClose}
            >
                {debug.map(({ id, name, iconUrl }) => (
                    <MenuItem
                        id={id}
                        onClick={handleSelectGuild}
                        selected={guild?.id === id}
                    >
                        <ListItemAvatar>
                            <Avatar
                                alt={name}
                                src={iconUrl || undefined}
                                className={classes.menuAvatar}
                            />
                        </ListItemAvatar>
                        <Typography variant="inherit">{name}</Typography>
                    </MenuItem>
                ))}
                <Divider />
                <MenuItem onClick={logOut}>
                    <ListItemIcon>
                        <ExitToAppIcon />
                    </ListItemIcon>
                    <Typography variant="inherit">Log out</Typography>
                </MenuItem>
            </Menu>
        </>
    )
}

const logOut = async () => {
    const res = await fetch('/logout', { method: 'POST' })
    if (res.redirected) {
        window.location.href = res.url
    }
}

export default Header