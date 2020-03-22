import React, { useEffect, useCallback } from 'react'
import styled from 'styled-components'

import { Button } from 'antd';
import { useDispatch, useSelector } from '@/util';
import { fetchSounds, playSound, playRandomSound } from '@/actions/sounds';

const SoundboardRoot = styled.div`
    display: flex;
    flex-wrap: wrap;
    justify-content: center;
`

const Sound = styled.div`
    margin: 5px;
    flex: 0 0 0;
`

const Soundboard: React.FC = () => {

    const { 
        selectedGuild, 
        sounds, 
        playingSound 
    } = useSelector((state) => ({
        selectedGuild: state.user.selectedGuild,
        sounds: state.sounds.sounds,
        playingSound: state.sounds.playingSound
    }))

    const dispatch = useDispatch()

    const onPlayRandomSound = useCallback(() => selectedGuild && dispatch(playRandomSound(selectedGuild)), [selectedGuild])

    useEffect(() => {
        selectedGuild && dispatch(fetchSounds(selectedGuild))
    }, [selectedGuild])

    return (
        <SoundboardRoot>
            {sounds.map((sound) => 
                <Sound key={sound.name}>
                    <Button
                        block
                        style={{ width: 120 }}
                        disabled={playingSound}
                        onClick={() => selectedGuild && dispatch(playSound(selectedGuild, sound.name))}>
                            <div style={{ overflow: "hidden", textOverflow: "ellipsis"}}>{sound.name}</div>
                    </Button>
                </Sound>
            )}
            <Sound>
                <Button
                    block
                    style={{ width: 120 }}
                    disabled={playingSound}
                    type="primary"
                    onClick={onPlayRandomSound}>
                        <div style={{ overflow: "hidden", textOverflow: "ellipsis"}}>Random</div>
                </Button>
            </Sound>
        </SoundboardRoot>
    )
}

export default Soundboard