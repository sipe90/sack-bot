import React from 'react'
import {
    TableOutlined,
    RobotOutlined,
    SoundOutlined
  } from '@ant-design/icons'
import styled from 'styled-components'

import Soundboard from '@/components/Soundboard'

export interface IRouteDefinition {
    path: string
    exact: boolean
    icon: JSX.Element
    text: string
    container: JSX.Element
}

const SoundBoardIcon = styled(TableOutlined)`
    font-size: 22px
`

const VoicesIcon = styled(SoundOutlined)`
    font-size: 22px
`

const TTSIcon = styled(RobotOutlined)`
    font-size: 22px
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
        container: <div />,
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