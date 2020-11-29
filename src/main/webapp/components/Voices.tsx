import React, { useState } from 'react'
import { useDispatch, useSelector } from '@/util'
import { playVoiceLines } from '@/actions/voices'
import { Box, Button, Chip, CircularProgress, createStyles, FormControl, InputLabel, makeStyles, MenuItem, Select } from '@material-ui/core'

const useStyles = makeStyles((theme) =>
    createStyles({
        formControl: {
            marginLeft: theme.spacing(1),
            marginRight: theme.spacing(1),
            minWidth: 180
        },
        buttonContainer: {
            alignSelf: 'flex-end',
            '& > *': {
                marginLeft: theme.spacing(0.5),
                marginRight: theme.spacing(0.5)
            }
        },
        chipContainer: {
            marginTop: theme.spacing(2),
            display: 'flex',
            flexWrap: 'wrap',
            '& > *': {
                marginLeft: theme.spacing(0.5),
                marginRight: theme.spacing(0.5)
            }
        }
    })
)

const LoadingIconComponent = (props: any) => <CircularProgress size={20} className={props.className} />

const Voices: React.FC = () => {

    const classes = useStyles()

    const selectedGuildId = useSelector((state) => state.user.selectedGuildId)
    const settings = useSelector((state) => state.settings.settings.voice)
    const voicesLoading = useSelector((state) => state.settings.settingsLoading)

    const { voices } = settings

    const dispatch = useDispatch()

    const [voice, setVoice] = useState<string>('')
    const [lines, setLines] = useState<string[]>([])

    return (
        <>
            <Box display='flex'>
                <Box flexGrow={1}>
                    <FormControl className={classes.formControl}>
                        <InputLabel id='voice-select-label'>{voicesLoading ? 'Loading voices...' : 'Select voice'}</InputLabel>
                        <Select
                            labelId='voice-select-label'
                            IconComponent={voicesLoading ? LoadingIconComponent : undefined}
                            value={voice}
                            onChange={(e) => {
                                setVoice(e.target.value as string)
                                setLines([])
                            }}
                        >
                            {Object.keys(voices).map((voice) =>
                                <MenuItem key={voice} value={voice}>{voice}</MenuItem>
                            )}
                        </Select>
                    </FormControl>
                    <FormControl className={classes.formControl}>
                        <InputLabel id='line-select-label'>Add line</InputLabel>
                        <Select
                            labelId='line-select-label'
                            value=''
                            disabled={!voice}
                            onChange={(e) => setLines(lines.concat(e.target.value as string))}
                        >
                            {voice && voices[voice].map((v) =>
                                <MenuItem key={v} value={v}>{v}</MenuItem>)}
                        </Select>
                    </FormControl>
                </Box>
                <Box className={classes.buttonContainer}>
                    <Button
                        variant='contained'
                        color='secondary'
                        onClick={() => setLines([])}
                    >
                        Clear
                    </Button>
                    <Button
                        variant='contained'
                        color='primary'
                        disabled={lines.length === 0 || !selectedGuildId}
                        onClick={() => selectedGuildId && voice && dispatch(playVoiceLines(selectedGuildId, voice, lines))}
                    >
                        Play
                    </Button>
                </Box>
            </Box>
            <div className={classes.chipContainer}>
                {lines.map((line, idx) =>
                    <Chip
                        key={idx}
                        size='small'
                        label={line}
                        onDelete={(e: Event) => {
                            e.preventDefault()
                            setLines(lines.filter((_line, i) => i !== idx))
                        }}
                    />
                )}
            </div>
        </>
    )
}

export default Voices