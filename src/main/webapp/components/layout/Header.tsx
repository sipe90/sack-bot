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
    Menu,
    MenuItem,
    Toolbar,
    Tooltip,
    Typography
} from '@mui/material'
import ExitToAppIcon from '@mui/icons-material/ExitToApp'
import MoreVertIcon from '@mui/icons-material/MoreVert'
import WbSunnyIcon from '@mui/icons-material/WbSunny'
import NightsStayIcon from '@mui/icons-material/NightsStay'

import { useSelector, useDispatch } from '@/util'
import { selectGuild } from '@/actions/user'
import { selectedGuild } from '@/selectors/user'

interface HeaderProps {
    darkMode: boolean
    onDarkModeChange: (enabled: boolean) => void
}

const Header: React.FC<HeaderProps> = (props) => {
    const user = useSelector((state) => state.user.userInfo)
    const guild = useSelector(selectedGuild)

    return (<>
        <Toolbar disableGutters>
            <Container sx={{ flexBasis: 0 }}>
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
                            variant='h6'
                            underline='hover'
                            sx={{
                                p: 1,
                                flexShrink: 0,
                                '&.active': {
                                    color: 'secondary.main',
                                    textDecoration: 'underline'
                                }
                            }}
                            to='board'
                        >
                            Board
                        </Link>
                        <Link
                            component={NavLink}
                            color='inherit'
                            noWrap
                            variant='h6'
                            underline='hover'
                            sx={{
                                p: 1,
                                flexShrink: 0,
                                '&.active': {
                                    color: 'secondary.main',
                                    textDecoration: 'underline'
                                }
                            }}
                            to='admin'
                        >
                            Admin
                        </Link>
                    </>
                }
            </Container>
            <Container sx={{ flexBasis: 0 }}>
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
            <Box display='flex' alignItems='center' sx={{
                '& > *': {
                    mr: 1,
                }
            }}>
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
                                sx={{ width: 32, height: 32 }}
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