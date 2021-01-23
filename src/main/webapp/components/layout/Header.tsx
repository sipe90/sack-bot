import React from 'react'
import { NavLink } from 'react-router-dom'
import {
    Avatar,
    Box,
    Container,
    Divider,
    IconButton,
    Link,
    ListItemAvatar,
    ListItemIcon,
    makeStyles,
    Menu,
    MenuItem,
    Toolbar,
    Typography
} from '@material-ui/core'
import ExitToAppIcon from '@material-ui/icons/ExitToApp'
import MoreVertIcon from '@material-ui/icons/MoreVert'

import { useSelector, useDispatch } from '@/util'
import { selectGuild } from '@/actions/user'
import { selectedGuild } from '@/selectors/user'

import sackbotAvatar from '@/public/img/Sackbot_V3_cropped.jpg'

const useStyles = makeStyles((theme) => ({
    toolbarTitle: {
        flexBasis: 0
    },
    avatar: {
        margin: theme.spacing(1),
    },
    toolbarLink: {
        padding: theme.spacing(1),
        flexShrink: 0,
    },
    activeLink: {
        color: theme.palette.primary.main
    },
    guildSelector: {
        '& > *': {
            marginRight: theme.spacing(1),
        }
    },
    menuAvatar: {
        width: theme.spacing(4),
        height: theme.spacing(4),
    },
}))

const Header: React.FC = () => {

    const classes = useStyles()

    const guild = useSelector(selectedGuild)

    return (<>
        <Toolbar disableGutters>
            <Container className={classes.toolbarTitle}>
                <Avatar className={classes.avatar} src={sackbotAvatar} />
            </Container>
            <Container component='nav'>
                <Link
                    component={NavLink}
                    color='inherit'
                    noWrap
                    variant='body2'
                    className={classes.toolbarLink}
                    to='/board'
                    exact
                    activeClassName={classes.activeLink}
                >
                    Board
                </Link>
                {!!guild?.isAdmin &&
                    <Link
                        component={NavLink}
                        color='inherit'
                        noWrap
                        variant='body2'
                        className={classes.toolbarLink}
                        to='/admin'
                        exact
                        activeClassName={classes.activeLink}
                    >
                        Admin
                </Link>
                }
            </Container>
            <GuildSelector />
        </Toolbar>
    </>)
}


const GuildSelector: React.FC = () => {

    const classes = useStyles()

    const guild = useSelector(selectedGuild)
    const guilds = useSelector((state) => state.user.guilds)

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
            <Box display='flex' alignItems='center' className={classes.guildSelector}>
                <Avatar
                    alt={guild?.name || 'G'}
                    src={guild?.iconUrl || undefined}
                />
                <IconButton
                    onClick={handleClickAvatar}
                >
                    <MoreVertIcon />
                </IconButton>
            </Box>
            <Menu
                anchorEl={anchorEl}
                keepMounted
                open={Boolean(anchorEl)}
                onClose={handleClose}
            >
                {guilds.map(({ id, name, iconUrl }) => (
                    <MenuItem
                        key={id}
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
                        <Typography variant='inherit'>{name}</Typography>
                    </MenuItem>
                ))}
                <Divider />
                <MenuItem onClick={logOut}>
                    <ListItemIcon>
                        <ExitToAppIcon />
                    </ListItemIcon>
                    <Typography variant='inherit'>Log out</Typography>
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