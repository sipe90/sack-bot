import React, { useState, useEffect } from 'react'

import { Button, Input, Spin, Select, Card } from 'antd'
import { useDispatch, useSelector } from '@/util'
import { playTTS, playRandomTTS, getVoices } from '@/actions/tts'

const { TextArea } = Input

const TTS: React.FC = () => {

    const selectedGuildId = useSelector((state) => state.user.selectedGuildId)
    const loadingVoices = useSelector((state) => state.tts.loadingVoices)
    const voices = useSelector((state) => state.tts.voices)

    const dispatch = useDispatch()

    useEffect(() => {
        selectedGuildId && dispatch(getVoices(selectedGuildId))
    }, [selectedGuildId])

    const [voice, setVoice] = useState<string>()
    const [text, setText] = useState<string>("")

    return (
        <Spin spinning={loadingVoices} tip='Loading voices...'>
            <Card
                title={
                    <div style={{ display: 'flex', whiteSpace: 'normal' }}>
                        <div style={{ flexGrow: 1 }}>
                            <Select<string>
                                style={{ width: 200, marginRight: 8, marginBottom: 8 }}
                                value={voice}
                                placeholder="Select voice"
                                onSelect={setVoice}
                            >
                                {voices.map((voice) =>
                                    <Select.Option key={voice} value={voice}>{voice}</Select.Option>)}
                            </Select>
                        </div>
                        <div>
                            <Button
                                style={{ width: 80, marginRight: 8, marginBottom: 8 }}
                                disabled={!voice || !selectedGuildId}
                                onClick={() => voice && selectedGuildId && dispatch(playRandomTTS(selectedGuildId, voice))}
                            >
                                Random
                            </Button>
                            <Button
                                style={{ width: 80 }}
                                type="primary"
                                disabled={!text.trim().length || !voice || !selectedGuildId}
                                onClick={() => voice && selectedGuildId && dispatch(playTTS(selectedGuildId, voice, text))}
                            >
                                Talk
                            </Button>
                        </div>
                    </div>
                }
            >
                <TextArea
                    allowClear
                    autoSize={{ minRows: 3 }}
                    value={text}
                    onChange={(e) => setText(e.target.value)}
                    maxLength={800}
                />

            </Card>
        </Spin>
    )
}

export default TTS