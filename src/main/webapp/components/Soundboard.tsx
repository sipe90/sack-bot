import React, { useEffect, useCallback } from 'react'
import styled from 'styled-components'
import * as R from 'ramda'
import { Button, Divider } from 'antd'

import { useDispatch, useSelector } from '@/util'
import { fetchSounds, playSound, playRandomSound } from '@/actions/sounds'
import { IAudioFile } from '@/types'

const SoundGrid = styled.div`
    display: flex;
    flex-wrap: wrap;
    justify-content: flex-start;
`

const Sound = styled.div`
    margin: 5px;
    flex: 0 0 0;
`

const groupByFirstLetter = R.groupBy<IAudioFile>(R.pipe(
    R.pathOr('', ['name', 0]),
    R.toUpper
))

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

    const groupedSounds = groupByFirstLetter(sounds)
    const letters = R.keys(groupedSounds).sort()

    return (
        <>
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
            {letters.map((letter) => 
                <div key={letter}>
                    <Divider>{letter}</Divider>
                    <SoundGrid>
                        {groupedSounds[letter].map((sound) => 
                            <Sound key={sound.name}>
                                <Button
                                    block
                                    style={{ width: 120 }}
                                    disabled={playingSound}
                                    onClick={() => selectedGuild && dispatch(playSound(selectedGuild, sound.name))}>
                                        <div style={{ overflow: "hidden", textOverflow: "ellipsis"}}>{sound.name}</div>
                                </Button>
                            </Sound>)}
                    </SoundGrid>
                </div>
            )}
        </>
    )
}

export default Soundboard