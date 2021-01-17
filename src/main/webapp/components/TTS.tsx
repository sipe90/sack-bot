import React, { useState } from 'react'

import { useDispatch, useSelector } from '@/util'
import { playTTS, playRandomTTS } from '@/actions/tts'
import { Box, Button, CircularProgress, createStyles, FormControl, InputLabel, makeStyles, MenuItem, Select, TextField } from '@material-ui/core'
import { useSnackbar } from 'notistack'

const useStyles = makeStyles((theme) =>
    createStyles({
        formControl: {
            marginTop: theme.spacing(2),
            width: '100%'
        },
        topContainer: {
            display: 'flex',
            '& > *': {
                marginLeft: theme.spacing(0.5),
                marginRight: theme.spacing(0.5)
            }
        },
        buttonContainer: {
            alignSelf: 'flex-end',
            '& > *': {
                marginLeft: theme.spacing(0.5),
                marginRight: theme.spacing(0.5)
            }
        }
    })
)

const LoadingIconComponent = (props: any) => <CircularProgress size={20} className={props.className} />

const TTS: React.FC = () => {

    const classes = useStyles()

    const selectedGuildId = useSelector((state) => state.user.selectedGuildId)
    const voicesLoading = useSelector((state) => state.settings.settingsLoading)
    const settings = useSelector((state) => state.settings.settings.tts)

    const { voices, randomEnabled, maxLength } = settings

    const dispatch = useDispatch()

    const { enqueueSnackbar } = useSnackbar()

    const [voice, setVoice] = useState<string>()
    const [text, setText] = useState<string>("")

    return (
        <>
            <div className={classes.topContainer}>
                <Box flexGrow={1}>
                    <FormControl className={classes.formControl}>
                        <InputLabel id='voice-select-label'>{voicesLoading ? 'Loading voices...' : 'Select voice'}</InputLabel>
                        <Select
                            labelId='voice-select-label'
                            IconComponent={voicesLoading ? LoadingIconComponent : undefined}
                            value={voice}
                            onChange={(e) => setVoice(e.target.value as string)}
                        >
                            {voices.map((voice) =>
                                <MenuItem key={voice} value={voice}>{voice}</MenuItem>)}
                        </Select>
                    </FormControl>
                </Box>
                <div className={classes.buttonContainer}>
                    {randomEnabled &&
                        <Button
                            variant='contained'
                            color='primary'
                            disabled={!voice || !selectedGuildId}
                            onClick={
                                () => voice && selectedGuildId &&
                                    dispatch(playRandomTTS(selectedGuildId, voice))
                                        .catch((err) => enqueueSnackbar('Failed to play text: ' + err.message, { variant: 'error' }))
                            }
                        >
                            Random
                        </Button>
                    }
                    <Button
                        variant='contained'
                        color='primary'
                        disabled={!text.trim().length || !voice || !selectedGuildId}
                        onClick={
                            () => voice && selectedGuildId &&
                                dispatch(playTTS(selectedGuildId, voice, text))
                                    .catch((err) => enqueueSnackbar('Failed to play text: ' + err.message, { variant: 'error' }))
                        }
                    >
                        Talk
                    </Button>
                </div>
            </div>
            <FormControl className={classes.formControl}>
                <TextField
                    fullWidth
                    multiline
                    rows={3}
                    variant='outlined'
                    onChange={(e) => setText(e.target.value)}
                    inputProps={{
                        maxLength
                    }}
                />
            </FormControl>
        </>
    )
}

export default TTS