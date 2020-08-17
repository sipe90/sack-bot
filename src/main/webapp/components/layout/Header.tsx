
import React from 'react'
import styled from 'styled-components'
import { Layout } from 'antd'

const { Header } = Layout

const HeaderWrapper = styled(Header)`
    height: 52px;
    padding: 0;
    line-height: normal;
`

const AppHeader: React.FC = ({ children }) => (
    <HeaderWrapper>
        {children}
    </HeaderWrapper>
)

export default AppHeader