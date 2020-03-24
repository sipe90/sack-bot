import React from 'react'
import {
    TableOutlined,
    RobotOutlined,
    SoundOutlined
  } from '@ant-design/icons'
import styled from 'styled-components'

import Soundboard from '@/components/Soundboard'
import Voices from './components/Voices'

export interface IRouteDefinition {
    path: string
    exact: boolean
    icon: JSX.Element
    text: string
    container: JSX.Element
}

const SoundBoardIcon = styled(TableOutlined)`
    margin-top: 8px;
    font-size: 24px
`

const VoicesIcon = styled(SoundOutlined)`
    margin-top: 8px;
    font-size: 24px
`

const TTSIcon = styled(RobotOutlined)`
    margin-top: 8px;
    font-size: 24px
`

const routes: IRouteDefinition[] = [
    {
        container: <Soundboard />,
        exact: true,
        icon: <SoundBoardIcon />,
        path: '/soundboard',
        text: 'Soundboard',
    },
    {
        container: <Voices />,
        exact: true,
        icon: <VoicesIcon />,
        path: '/voices',
        text: 'Voices',
    },
    {
        container: <div />,
        exact: true,
        icon: <TTSIcon />,
        path: '/tts',
        text: 'Text to speech',
    }
]

export default routes