import React, { useCallback } from 'react'
import { NavLink } from 'react-router-dom'
import styled from 'styled-components'

import { IRouteDefinition } from '@/routeDefs'
import { Avatar, Select } from 'antd'
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
        <Select<string>
            style={{ minWidth: 200, marginRight: 10 }}
            placeholder="Select guild"
            value={selectedGuild || undefined}
            onSelect={(guildId) => dispatch(selectGuild(guildId))}
        >
            {guilds.map(({ id, name}) => (
                <Select.Option
                    key={id}
                    value={id}
                >
                    {name}
            </Select.Option>)
            )}
        </Select>
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
        <div>
            {route.icon}
        </div>
        <div>
            {route.text}
        </div>
    </NavigationLink>
)

export default Navigation