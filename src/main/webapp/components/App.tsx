import React from 'react'
import { Select, Avatar } from 'antd'

import { IGuild, IAudioFile } from '@/types'
import SoundBoard from '@/components/SoundBoard'

interface IAppProps {
    guilds: IGuild[]
    selectedGuild: string | null
    onGuildSelect: (guildId: string) => void
    sounds: IAudioFile[]
    onPlaySound: (name: string) => void
    onPlayRandomSound: () => void
    playingSound: boolean
}

const App: React.FC<IAppProps> = (props) =>  (
    <div
        style={{
            backgroundColor: "#5dabcf"
        }}
    >
        <div
            style={{
                backgroundColor: "rgb(255, 255, 255)",
                boxShadow: "0 4px 8px 0 rgba(0, 0, 0, 0.2), 0 6px 20px 0 rgba(0, 0, 0, 0.19)",
                minHeight: "100vh",
                maxWidth: "1080px",
                margin: "auto"
            }}
        >
            <Avatar
                size="large"
                src={props.selectedGuild ? props.guilds.find(({id}) => id == props.selectedGuild)?.iconUrl || undefined : undefined}>
                    {props.selectedGuild ? undefined : "G"}
            </Avatar>
            <Select<string>
                value={props.selectedGuild || undefined}
                onSelect={props.onGuildSelect}
            >
                {props.guilds.map(({ id, name}) => (
                <Select.Option
                    key={id}
                    value={id}
                >
                    {name}
                </Select.Option>)
                )}
            </Select>
            <SoundBoard
                sounds={props.sounds}
                onPlaySound={props.onPlaySound}
                onPlayRandomSound={props.onPlayRandomSound}
                playingSound={props.playingSound}
            />
        </div>
    </div>
)

export default App