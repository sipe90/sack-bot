import React, { useState } from 'react'

import { Button, Input } from 'antd'
import { useDispatch, useSelector } from '@/util'
import { playTTS, playRandomTTS } from '@/actions/tts'

const { TextArea } = Input

const TTS: React.FC = () => {

    const { 
        selectedGuild, 
        playingTTS 
    } = useSelector((state) => ({
        selectedGuild: state.user.selectedGuild,
        playingTTS: state.tts.playingTTS
    }))

    const dispatch = useDispatch()

    const [text, setText] = useState<string>("")

    return (
        <>
            <TextArea
                allowClear
                value={text}
                onChange={(e) => setText(e.target.value)}
            />
            <div style={{ display: 'flex', whiteSpace: 'normal', marginTop: 8 }}>
                <div style={{ flexGrow: 1 }}>
                </div>
                <div>
                <Button
                    style={{ width: 80, marginRight: 8, marginBottom: 8 }}
                    disabled={playingTTS || !selectedGuild}
                    onClick={() => selectedGuild && dispatch(playRandomTTS(selectedGuild))}
                >
                    Random
                </Button>
                <Button 
                    style={{ width: 80 }}
                    type="primary"
                    disabled={playingTTS || !selectedGuild}
                    onClick={() => selectedGuild && dispatch(playTTS(selectedGuild, text))}
                >
                    Talk
                </Button>
                </div>
            </div>
        </>
    )
}

export default TTS