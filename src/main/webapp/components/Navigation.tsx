import React from 'react'
import { NavLink } from 'react-router-dom'
import styled from 'styled-components'

import { IRouteDefinition } from '@/routeDefs'
import { Avatar, Button, Menu, Dropdown } from 'antd'
import { DownOutlined } from '@ant-design/icons';
import { useSelector, useDispatch } from '@/util'
import { selectGuild } from '@/actions/user'

interface INavigationProps {
    routes: IRouteDefinition[]
}

const NavigationMenu = styled.div`
    text-align: center;
    flex-grow: 1;
`

const NavigationLink = styled(NavLink)`
    display: inline-block;
    height: 52px;
    padding: 6px 16px 2px 16px;
    color: rgba(255, 255, 255, 0.80);
`

const activeStyle: React.CSSProperties = {
    backgroundColor: "#1890ff",
    color: "#fff",
    textDecoration: "none",
    borderBottom: "2px solid #fff"
}

const Navigation: React.FC<INavigationProps> = (props) => {
    
    const selectedGuild = useSelector((state) => state.user.selectedGuild)
    const guilds = useSelector((state) => state.user.guilds)
    const dispatch = useDispatch()

    return (
    <div style={{ display: "flex", justifyContent: "center" }}>
        <NavigationMenu>
            {props.routes.map(renderNavigationLink)}
        </NavigationMenu>
        <div style={{ display: "flex", alignItems: "center" }}>
            <Avatar
                style={{ marginRight: 10 }}
                size="large"
                src={selectedGuild ? guilds.find(({id}) => id == selectedGuild)?.iconUrl || undefined : undefined}>
                    {selectedGuild ? undefined : "G"}
            </Avatar>
            <div style={{marginRight: 10 }}>
                <Dropdown
                    overlay={
                        <Menu
                            selectedKeys={selectedGuild ?[selectedGuild] : []}
                        >
                            {guilds.map(({ id, name }) => (
                                <Menu.Item
                                    key={id}
                                    onClick={() => dispatch(selectGuild(id))}
                                >
                                    {name}
                                </Menu.Item>)
                            )}
                        </Menu>
                    }
                >
                    <Button>
                        Guild <DownOutlined />
                    </Button>
                </Dropdown>
            </div>
        </div>
    </div>
)
}

const renderNavigationLink = (route: IRouteDefinition, index: number) => (
    <NavigationLink
        activeStyle={activeStyle}
        key={index}
        to={route.path}
        exact={true}
    >
            {route.icon}
    </NavigationLink>
)

export default Navigation