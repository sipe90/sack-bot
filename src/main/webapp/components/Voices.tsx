import React, { useState } from 'react'
import styled from 'styled-components'

import { Button, Tag, Select, Card, Spin } from 'antd'
import { useDispatch, useSelector } from '@/util'
import { playVoiceLines } from '@/actions/voices'

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

    const selectedGuildId = useSelector((state) => state.user.selectedGuildId)
    const settings = useSelector((state) => state.settings.settings.voice)
    const voicesLoading = useSelector((state) => state.settings.settingsLoading)

    const { voices } = settings

    const dispatch = useDispatch()

    const [voice, setVoice] = useState<string>()
    const [lines, setLines] = useState<string[]>([])

    return (
        <Spin tip='Loading voices...' spinning={voicesLoading}>
            <Card
                bodyStyle={{ height: '100%' }}
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
                                {Object.keys(voices).map((voice) =>
                                    <Select.Option key={voice} value={voice}>{voice}</Select.Option>)}
                            </Select>
                            <Select<string>
                                style={{ width: 200 }}
                                value=""
                                disabled={!voice}
                                showSearch
                                onSelect={(value) => setLines(lines.concat(value))}
                            >
                                {voice && voices[voice].map((v) =>
                                    <Select.Option key={v} value={v}>{v}</Select.Option>)}
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
                                disabled={lines.length === 0 || !selectedGuildId}
                                onClick={() => selectedGuildId && voice && dispatch(playVoiceLines(selectedGuildId, voice, lines))}
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
                            onClose={(e: Event) => {
                                e.preventDefault()
                                setLines(lines.filter((_line, i) => i !== idx))
                            }}
                        >
                            {line}
                        </VoiceLine>
                    )}
                </VoiceLinesContainer>
            </Card>
        </Spin>
    )
}

export default Voices