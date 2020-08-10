import React, { useEffect, useCallback, useMemo, useState } from 'react'
import styled from 'styled-components'
import * as R from 'ramda'
import { Button, Divider, Dropdown, Input, Menu, Select, Slider } from 'antd'
import { SoundOutlined, PlayCircleOutlined } from '@ant-design/icons'

import { useDispatch, useSelector } from '@/util'
import { fetchSounds, playSound, playRandomSound, playUrl } from '@/actions/sounds'
import { IAudioFile } from '@/types'
import { selectedGuildMembership } from "@/selectors/user"
import { updateEntrySound, updateExitSound } from "@/actions/user"

const { Search } = Input

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

const defVolume = 75

const Soundboard: React.FC = () => {

    const selectedGuild = useSelector((state => state.user.selectedGuildId))
    const sounds = useSelector((state => state.sounds.sounds))
    const membership = useSelector(selectedGuildMembership)

    const dispatch = useDispatch()

    const [volume, setVolume] = useState<number>(defVolume)
    const [url, setUrl] = useState<string>("")
    const [tagFilter, setTagFilter] = useState<string[]>([])

    const onPlayRandomSound = useCallback(() => selectedGuild && dispatch(playRandomSound(selectedGuild, volume, tagFilter)), [selectedGuild, volume, tagFilter])

    const onPlaySound = useCallback((sound: string) => selectedGuild && dispatch(playSound(selectedGuild, sound, volume)), [selectedGuild, volume])
    const onPlayUrl = useCallback((url: string) => selectedGuild && dispatch(playUrl(selectedGuild, url, volume)), [selectedGuild, volume])
    const onUpdateEntrySound = useCallback((sound: string) => selectedGuild && dispatch(updateEntrySound(selectedGuild, sound)), [selectedGuild])
    const onUpdateExitSound = useCallback((sound: string) => selectedGuild && dispatch(updateExitSound(selectedGuild, sound)), [selectedGuild])
    const onClearEntrySound = useCallback(() => selectedGuild && dispatch(updateEntrySound(selectedGuild)), [selectedGuild])
    const onClearExitSound = useCallback(() => selectedGuild && dispatch(updateExitSound(selectedGuild)), [selectedGuild])

    useEffect(() => {
        selectedGuild && dispatch(fetchSounds(selectedGuild))
    }, [selectedGuild])

    const tags = useMemo(() => getTags(sounds), [sounds])

    return (
        <>
            <div style={{ display: 'flex', alignItems: 'center', marginBottom: 8 }}>
                <div style={{ marginRight: 8 }}>
                    <SoundOutlined style={{ fontSize: 14 }} />
                </div>
                <div style={{ flexGrow: 1 }}>
                    <Slider defaultValue={defVolume} min={1} max={100} onAfterChange={(vol) => setVolume(vol as number)} />
                </div>
            </div>
            <div>
                <Search
                    placeholder="Play from URL"
                    enterButton={<PlayCircleOutlined style={{ fontSize: 14 }} />}
                    value={url}
                    onChange={(e) => setUrl(e.target.value)}
                    onSearch={onPlayUrl}
                />
            </div>
            <Divider></Divider>
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
                    type="primary"
                    onClick={onPlayRandomSound}
                >
                    <div style={{ overflow: "hidden", textOverflow: "ellipsis" }}>Random</div>
                </Button>
            </div>
            <Grid
                sounds={sounds}
                tagFilter={tagFilter}
                entrySound={membership?.entrySound || null}
                exitSound={membership?.exitSound || null}
                onPlaySound={onPlaySound}
                onUpdateEntrySound={onUpdateEntrySound}
                onUpdateExitSound={onUpdateExitSound}
                onClearEntrySound={onClearEntrySound}
                onClearExitSound={onClearExitSound}
            />
        </>
    )
}

const SoundGrid = styled.div`
    display: flex;
    flex-wrap: wrap;
    justify-content: flex-start;
`

const Sound = styled.div`
    margin: 5px;
    flex: 0 0 0;
`

interface IGripProps {
    sounds: IAudioFile[]
    tagFilter: string[]
    entrySound: string | null
    exitSound: string | null
    onPlaySound: (sound: string) => void
    onUpdateEntrySound: (sound: string) => void
    onUpdateExitSound: (sound: string) => void
    onClearEntrySound: () => void
    onClearExitSound: () => void
}

const Grid: React.FC<IGripProps> = React.memo((props) => {

    const {
        sounds,
        tagFilter,
        entrySound,
        exitSound,
        onPlaySound,
        onUpdateEntrySound,
        onUpdateExitSound,
        onClearEntrySound,
        onClearExitSound
    } = props

    const groupedSounds = useMemo(() => filterAndGroup(sounds, tagFilter), [sounds, tagFilter])
    const letters = useMemo(() => R.keys(groupedSounds).sort(), [groupedSounds])

    return (
        <>
            {letters.map((letter) =>
                <div key={letter}>
                    <Divider>{letter}</Divider>
                    <SoundGrid>
                        {groupedSounds[letter].map(({ name }) =>
                            <Sound key={name}>
                                <Dropdown
                                    trigger={['contextMenu']}
                                    overlay={
                                        <Menu>
                                            <Menu.Item onClick={() => onUpdateEntrySound(name)}>Set as entry sound</Menu.Item>
                                            <Menu.Item onClick={() => onUpdateExitSound(name)}>Set as exit sound</Menu.Item>
                                            {entrySound && <Menu.Item onClick={onClearEntrySound}>Clear entry sound {` (${entrySound})`}</Menu.Item>}
                                            {exitSound && <Menu.Item onClick={onClearExitSound}>Clear exit sound {` (${exitSound})`}</Menu.Item>}
                                        </Menu>
                                    }
                                >
                                    <Button
                                        block
                                        style={{ width: 120 }}
                                        onClick={() => onPlaySound(name)}>
                                        <div style={{ overflow: "hidden", textOverflow: "ellipsis" }}>{name}</div>
                                    </Button>
                                </Dropdown>
                            </Sound>)}
                    </SoundGrid>
                </div>
            )}
        </>
    )
})

export default Soundboard