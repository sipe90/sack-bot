import React from 'react'
import { Redirect, Route, Switch } from 'react-router'
import styled from 'styled-components'

import { IRouteDefinition } from '@/routeDefs'

interface IRoutesProps {
    routes: IRouteDefinition[]
}

const RouteWrapper = styled.div`
    margin-left: auto;
    margin-right: auto;
    padding: 6px;
`

const Routes: React.FC<IRoutesProps> = (props) => {
    return (
        <Switch>
            <Route
                exact={true}
                path='/'
                component={() => <Redirect to={props.routes[0].path} />}
            />
            {props.routes.map(({ path, exact, container }, index) => (
                <Route
                    key={index}
                    path={path}
                    exact={exact}
                    component={() => <RouteWrapper>{container}</RouteWrapper>}
                />
            ))}
            {<RouteWrapper>404</RouteWrapper>}
        </Switch>
    )
}

export default Routes