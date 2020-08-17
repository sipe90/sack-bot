import React from 'react'
import styled from 'styled-components'
import { Card, Layout } from 'antd'

const { Content } = Layout

const ContentWrapper = styled(Content)`
    display: flex;
`

const ContentCard = styled(Card)`
    flex-grow: 1;
    border-radius: 0;
`

const AppContent: React.FC = ({ children }) => (
    <ContentWrapper>
        <ContentCard
            bordered={false}
            bodyStyle={{ height: '100%' }}
        >
            {children}
        </ContentCard>
    </ContentWrapper>
)

export default AppContent