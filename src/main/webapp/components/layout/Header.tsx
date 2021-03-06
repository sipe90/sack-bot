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
    Tooltip,
    Typography
} from '@material-ui/core'
import ExitToAppIcon from '@material-ui/icons/ExitToApp'
import MoreVertIcon from '@material-ui/icons/MoreVert'
import WbSunnyIcon from '@material-ui/icons/WbSunny'
import NightsStayIcon from '@material-ui/icons/NightsStay'

import { useSelector, useDispatch } from '@/util'
import { selectGuild } from '@/actions/user'
import { selectedGuild } from '@/selectors/user'

const useStyles = makeStyles((theme) => ({
    toolbarTitle: {
        flexBasis: 0
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
    }
}))

interface HeaderProps {
    darkMode: boolean
    onDarkModeChange: (enabled: boolean) => void
}

const Header: React.FC<HeaderProps> = (props) => {

    const classes = useStyles()

    const user = useSelector((state) => state.user.userInfo)
    const guild = useSelector(selectedGuild)

    return (<>
        <Toolbar disableGutters>
            <Container className={classes.toolbarTitle}>
                <Avatar
                    alt={user?.name || 'U'}
                    src={user?.avatarUrl || undefined}
                />
            </Container>
            <Container component='nav'>
                {!!guild?.isAdmin &&
                    <>
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
                    </>
                }
            </Container>
            <Container className={classes.toolbarTitle}>
                <Tooltip title='Toggle dark mode'>
                    <IconButton
                        onClick={() => props.onDarkModeChange(!props.darkMode)}
                    >
                        {props.darkMode ? <NightsStayIcon /> : <WbSunnyIcon />}
                    </IconButton>
                </Tooltip>
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
                <Tooltip title={guild?.name || ''}>
                    <Avatar
                        alt={guild?.name || 'G'}
                        src={guild?.iconUrl || undefined}
                    />
                </Tooltip>
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