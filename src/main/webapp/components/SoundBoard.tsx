import React from 'react'

import { IAudioFile } from "@/types";
import { Button } from 'antd';

interface ISoundBoardProps {
    sounds: IAudioFile[]
    onPlaySound: (name: string) => void
    onPlayRandomSound: () => void
    playingSound: boolean
}

const SoundBoard: React.FC<ISoundBoardProps> = (props) =>  (
    <div style={{ display: "flex", flexWrap: "wrap", justifyContent: "center" }}>
        {props.sounds.map((sound) => <div key={sound.name} style={{ margin: 5 }}><Button disabled={props.playingSound} onClick={() => props.onPlaySound(sound.name)}>{sound.name}</Button></div>)}
        <div style={{ margin: 5 }}><Button disabled={props.playingSound} type="primary" onClick={() => props.onPlayRandomSound()}>Random</Button></div>
    </div>
)

export default SoundBoard