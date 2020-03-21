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
                        disabled={playingSound}
                        onClick={() => selectedGuild && dispatch(playSound(selectedGuild, sound.name))}>
                            {sound.name}
                    </Button>
                </Sound>
            )}
            <Sound>
                <Button
                    disabled={playingSound}
                    type="primary"
                    onClick={onPlayRandomSound}>
                        Random
                </Button>
            </Sound>
        </SoundboardRoot>
    )
}

export default Soundboard