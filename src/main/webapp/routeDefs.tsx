import React from 'react'
import {
    TableOutlined,
    RobotOutlined,
    SoundOutlined,
    SettingOutlined
  } from '@ant-design/icons'
import styled, { AnyStyledComponent } from 'styled-components'

import Soundboard from '@/components/Soundboard'
import Voices from '@/components/Voices'
import TTS from '@/components/TTS'
import Admin from '@/components/Admin'

export interface IRouteDefinition {
    path: string
    exact: boolean
    icon: JSX.Element
    text: string
    container: JSX.Element
}

const styledIcon = (icon: AnyStyledComponent) => styled(icon)`
    margin-top: 8px;
    font-size: 24px;
`

const routes: IRouteDefinition[] = [
    {
        container: <Soundboard />,
        exact: true,
        icon: React.createElement(styledIcon(TableOutlined)),
        path: '/soundboard',
        text: 'Soundboard',
    },
    {
        container: <Voices />,
        exact: true,
        icon: React.createElement(styledIcon(SoundOutlined)),
        path: '/voices',
        text: 'Voices',
    },
    {
        container: <TTS />,
        exact: true,
        icon: React.createElement(styledIcon(RobotOutlined)),
        path: '/tts',
        text: 'Text to speech',
    },
    {
        container: <Admin />,
        exact: true,
        icon: React.createElement(styledIcon(SettingOutlined)),
        path: '/admin',
        text: 'Admin',
    }
]

export default routes