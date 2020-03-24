import React, { useEffect, useState } from 'react'
import styled from 'styled-components'

import { Button, Tag, Select } from 'antd'
import { useDispatch, useSelector } from '@/util'
import { fetchVoiceLines, playVoiceLines } from '@/actions/voices'

const Voices: React.FC = () => {

    const { 
        selectedGuild, 
        voiceLines, 
        playingVoiceLines 
    } = useSelector((state) => ({
        selectedGuild: state.user.selectedGuild,
        voiceLines: state.voices.voiceLines,
        playingVoiceLines: state.voices.playingVoiceLines
    }))

    const dispatch = useDispatch()

    const [voice, setVoice] = useState<string>()
    const [lines, setLines] = useState<string[]>([])

    useEffect(() => {
        dispatch(fetchVoiceLines())
    }, [])

    return (
        <>
            <Select<string>
                style={{ width: 200 }}
                value={voice}
                placeholder="Select voice"
                onSelect={(value) => {
                    setVoice(value)
                    setLines([])
                }}
            >
                {Object.keys(voiceLines).map((voice) =>
                    <Select.Option value={voice}>{voice}</Select.Option>)}
            </Select>
            <Select<string>
                style={{ width: 200 }}
                value={null as any}
                disabled={!voice}
                showSearch
                onSelect={(value) => setLines(lines.concat(value))}
            >
                {voice && voiceLines[voice].map((voiceLine) =>
                    <Select.Option value={voiceLine}>{voiceLine}</Select.Option>)}
            </Select>
            <Button
                onClick={() => setLines([])}
            >
                Clear
            </Button>
            <Button
                type="primary"
                disabled={playingVoiceLines || !selectedGuild}
                onClick={() => selectedGuild && voice && dispatch(playVoiceLines(selectedGuild, voice, lines))}
            >
                Play
            </Button>
            <div>
            {lines.map((line, idx) =>
                <Tag key={idx} closable onClose={(e: Event) => setLines(lines.filter((_line, i) => { e.preventDefault(); return i !== idx}))}>
                    {line}
                </Tag>
            )}
            </div>
        </>
    )
}

export default Voices