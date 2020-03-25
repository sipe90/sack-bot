import React, { useEffect, useState } from 'react'
import styled from 'styled-components'

import { Button, Tag, Select, Card } from 'antd'
import { useDispatch, useSelector } from '@/util'
import { fetchVoiceLines, playVoiceLines } from '@/actions/voices'

const VoiceLinesContainer = styled.div`
    display: flex;
    flex-wrap: wrap;
    justify-content: flex-start;
`

const VoiceLine = styled(Tag)`
    margin: 5px;
    flex: 0 0 0;
`

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
        <Card
            headStyle={{ overflow: 'visible' }}
            bodyStyle={{ minHeight: 80 }}
            title={
                <div style={{ display: 'flex', whiteSpace: 'normal' }}>
                    <div style={{ flexGrow: 1 }}>
                        <Select<string>
                            style={{ width: 200, marginRight: 8, marginBottom: 8 }}
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
                    </div>
                    <div>
                    <Button
                        style={{ width: 60, marginRight: 8, marginBottom: 8 }}
                        onClick={() => setLines([])}
                    >
                        Clear
                    </Button>
                    <Button 
                        style={{ width: 60 }}
                        type="primary"
                        disabled={playingVoiceLines || !selectedGuild}
                        onClick={() => selectedGuild && voice && dispatch(playVoiceLines(selectedGuild, voice, lines))}
                    >
                        Play
                    </Button>
                    </div>
                </div>
            }
        >
            <VoiceLinesContainer>
                {lines.map((line, idx) =>
                    <VoiceLine 
                        key={idx} 
                        closable 
                        onClose={(e: Event) => 
                            setLines(lines.filter((_line, i) => { 
                                e.preventDefault() 
                                return i !== idx
                            }
                        )
                    )}>
                        {line}
                    </VoiceLine>
                )}
            </VoiceLinesContainer>
        </Card>
    )
}

export default Voices