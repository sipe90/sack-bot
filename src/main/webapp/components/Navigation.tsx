import React from 'react'
import { NavLink } from 'react-router-dom'
import styled, { AnyStyledComponent } from 'styled-components'
import { Avatar, Menu, Dropdown } from 'antd'
import {
    CaretDownFilled,
    TableOutlined,
    RobotOutlined,
    SoundOutlined,
    SettingOutlined
} from '@ant-design/icons'


import { useSelector, useDispatch } from '@/util'
import { selectGuild } from '@/actions/user'
import { selectedGuild } from '@/selectors/user'

const NavigationMenu = styled.div`
    flex-grow: 1;
`

const NavigationLink = styled(NavLink)`
    display: inline-block;
    height: 52px;
    padding: 6px 10px 6px 10px;
    color: rgba(255, 255, 255, 0.80);
`

const activeStyle: React.CSSProperties = {
    backgroundColor: "#1890ff",
    color: "#fff",
    textDecoration: "none",
    borderBottom: "2px solid #fff"
}

const styledIcon = (icon: AnyStyledComponent) => styled(icon)`
    margin-top: 8px;
    font-size: 24px;
`

const BoardIcon = styledIcon(TableOutlined)
const VoicesIcon = styledIcon(SoundOutlined)
const TTSIcon = styledIcon(RobotOutlined)
const AdminIcon = styledIcon(SettingOutlined)

const Navigation: React.FC = () => {

    const guild = useSelector(selectedGuild)
    const guilds = useSelector((state) => state.user.guilds)
    const dispatch = useDispatch()

    return (
        <div style={{ display: "flex", justifyContent: "center" }}>
            <NavigationMenu>
                <NavigationLink
                    activeStyle={activeStyle}
                    to='/board'
                    exact
                >
                    <BoardIcon />
                </NavigationLink>
                <NavigationLink
                    activeStyle={activeStyle}
                    to='/voices'
                    exact
                >
                    <VoicesIcon />
                </NavigationLink>
                <NavigationLink
                    activeStyle={activeStyle}
                    to='/tts'
                    exact
                >
                    <TTSIcon />
                </NavigationLink>
                <NavigationLink
                    activeStyle={activeStyle}
                    to='/admin'
                    exact
                >
                    <AdminIcon />
                </NavigationLink>
            </NavigationMenu>
            <div style={{ display: "flex", alignItems: "center", marginRight: 10 }}>
                <Dropdown
                    placement="bottomRight"
                    overlay={
                        <Menu
                            selectedKeys={guild ? [guild.id] : []}
                        >
                            {guilds.map(({ id, name, iconUrl }) => (
                                <Menu.Item
                                    key={id}
                                    onClick={() => dispatch(selectGuild(id))}
                                >
                                    <Avatar
                                        style={{ marginRight: 10 }}
                                        size={24}
                                        src={iconUrl || undefined}
                                    />
                                    {name}
                                </Menu.Item>)
                            )}
                            <Menu.Divider />
                            <Menu.Item
                                onClick={logOut}
                            >
                                Log out
                            </Menu.Item>
                        </Menu>
                    }
                >
                    <div>
                        <Avatar
                            style={{ marginRight: 10 }}
                            size={34}
                            src={guild?.iconUrl || undefined}
                        />
                        <CaretDownFilled
                            style={{ color: "#fff" }}
                        />
                    </div>
                </Dropdown>
            </div>
        </div>
    )
}

const logOut = async () => {
    const res = await fetch('/logout', { method: 'POST' })
    if (res.redirected) {
        window.location.href = res.url
    }
}

export default Navigation