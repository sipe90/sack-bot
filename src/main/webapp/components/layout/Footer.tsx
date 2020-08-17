
import React from 'react'
import styled from 'styled-components'

import { Card, Layout } from 'antd'

const { Footer } = Layout

const FooterWrapper = styled(Footer)`
    padding: 0px 0px 4px 0px;
    text-align: center;
`

const FooterCard = styled(Card)`
    border-radius: 0;
`

const AppFooter: React.FC = ({ children }) => (
    <FooterWrapper>
        <FooterCard bordered={false}>
            {children}
        </FooterCard>
    </FooterWrapper>
)

export default AppFooter