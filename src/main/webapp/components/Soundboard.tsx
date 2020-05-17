import React, { useEffect, useCallback, useMemo, useState } from 'react'
import styled from 'styled-components'
import * as R from 'ramda'
import { Button, Divider, Dropdown, Menu, Select, Slider } from 'antd'
import { SoundOutlined } from '@ant-design/icons'

import { useDispatch, useSelector } from '@/util'
import { fetchSounds, playSound, playRandomSound } from '@/actions/sounds'
import { IAudioFile } from '@/types'
import {selectedGuildMembership} from "@/selectors/user";
import {updateEntrySound, updateExitSound} from "@/actions/user";

const SoundGrid = styled.div`
    display: flex;
    flex-wrap: wrap;
    justify-content: flex-start;
`

const Sound = styled.div`
    margin: 5px;
    flex: 0 0 0;
`
const isSubset = R.curry((xs: any[], ys: any[]) =>
	R.all(R.contains(R.__, ys), xs))

const filterAndGroup = R.pipe(
    (sounds: IAudioFile[], tagFilter: string[]) => tagFilter.length ? sounds.filter((sound) => isSubset(tagFilter, sound.tags)) : sounds,
    R.groupBy<IAudioFile>(
        R.pipe(
            R.pathOr('', ['name', 0]),
            R.toUpper
        )
    )
)

const getTags = R.pipe<IAudioFile[], string[], string[], string[]>(
    R.chain<IAudioFile, string>(R.prop('tags')),
    R.uniq,
    R.invoker(0, 'sort')
)

const Soundboard: React.FC = () => {

    const selectedGuild = useSelector((state => state.user.selectedGuild))
    const sounds = useSelector((state => state.sounds.sounds))
    const playingSound = useSelector((state => state.sounds.playingSound))
    const membership = useSelector(selectedGuildMembership)

    const dispatch = useDispatch()

    const [volume, setVolume] = useState<number>(75)
    const [tagFilter, setTagFilter] = useState<string[]>([])

    const onPlayRandomSound = useCallback(() => selectedGuild && dispatch(playRandomSound(selectedGuild, volume, tagFilter)), [selectedGuild, volume, tagFilter])

    useEffect(() => {
        selectedGuild && dispatch(fetchSounds(selectedGuild))
    }, [selectedGuild])

    const groupedSounds = useMemo(() => filterAndGroup(sounds, tagFilter), [sounds, tagFilter])
    const letters = useMemo(() => R.keys(groupedSounds).sort(), [groupedSounds])
    const tags = useMemo(() => getTags(sounds), [sounds])

    return (
        <>
            <div style={{ display: 'flex', alignItems: 'center' }}>
                <div style={{ marginRight: 8 }}>
                    <SoundOutlined style={{ fontSize: 18 }}/>
                </div>
                <div style={{ flexGrow: 1 }}>
                    <Slider value={volume} min={1} max={100} onChange={(vol) => setVolume(vol as number)} />
                </div>
            </div>
            <div style={{ display: 'flex' }}>
                <div style={{ flexGrow: 1 }}>
                <Select
                    style={{ width: '100%' }}
                    mode='multiple'
                    allowClear
                    placeholder='Filter by tags'
                    value={tagFilter}
                    onChange={setTagFilter}
                >
                    {tags.map((tag) => <Select.Option key={tag} value={tag}>{tag}</Select.Option>)}
                </Select>
                </div>
                <Button
                    block
                    style={{ marginLeft: 8, width: 120 }}
                    disabled={playingSound}
                    type="primary"
                    onClick={onPlayRandomSound}>
                        <div style={{ overflow: "hidden", textOverflow: "ellipsis"}}>Random</div>
                </Button>
            </div>
            {letters.map((letter) => 
                <div key={letter}>
                    <Divider>{letter}</Divider>
                    <SoundGrid>
                        {groupedSounds[letter].map((sound) => 
                            <Sound key={sound.name}>
                                <Dropdown
                                    trigger={['contextMenu']}
                                    overlay={
                                        <Menu>
                                            <Menu.Item onClick={() => selectedGuild && dispatch(updateEntrySound(selectedGuild, sound.name))}>Set as entry sound</Menu.Item>
                                            <Menu.Item onClick={() => selectedGuild && dispatch(updateExitSound(selectedGuild, sound.name))}>Set as exit sound</Menu.Item>
                                            {membership?.entrySound && <Menu.Item onClick={() => selectedGuild && dispatch(updateEntrySound(selectedGuild))}>Clear entry sound {` (${membership.entrySound})`}</Menu.Item>}
                                            {membership?.exitSound && <Menu.Item onClick={() => selectedGuild && dispatch(updateExitSound(selectedGuild))}>Clear exit sound {` (${membership.exitSound})`}</Menu.Item>}
                                        </Menu>
                                    }
                                >
                                    <Button
                                        block
                                        style={{ width: 120 }}
                                        disabled={playingSound}
                                        onClick={() => selectedGuild && dispatch(playSound(selectedGuild, sound.name, volume))}>
                                            <div style={{ overflow: "hidden", textOverflow: "ellipsis"}}>{sound.name}</div>
                                    </Button>
                                </Dropdown>
                            </Sound>)}
                    </SoundGrid>
                </div>
            )}
        </>
    )
}

export default Soundboard