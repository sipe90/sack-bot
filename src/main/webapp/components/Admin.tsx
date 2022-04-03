import React, { useEffect, useMemo, useState } from 'react'
import * as R from 'ramda'
import { Button, Dialog, DialogActions, DialogContent, DialogContentText, DialogTitle, Paper, TextField } from '@mui/material'
import Autocomplete from '@mui/material/Autocomplete'
import { useSnackbar } from 'notistack'
import { Navigate } from 'react-router-dom'

import { useDispatch, useSelector } from '@/util'
import { fetchGuildMembers } from '@/actions/user'
import { fetchSounds, deleteSound, playSound, updateSound } from '@/actions/sounds'
import { IAudioFile } from '@/types'
import { selectedGuild, selectedGuildMembers } from '@/selectors/user'
import SoundsTable from '@/components/SoundsTable'

const getTags = R.pipe<[IAudioFile[]], string[], string[], string[]>(
    R.chain<IAudioFile, string>(R.prop('tags')),
    R.uniq,
    R.invoker(0, 'sort')
)

const downloadZip = (guildId: string) => downloadFile(`/api/${guildId}/sounds/export`)
const downloadSound = (guildId: string, name: string) => downloadFile(`/api/${guildId}/sounds/${name}/download`)

const downloadFile = (url: string) => {
    const a = document.createElement('a')
    a.href = url
    document.body.appendChild(a)
    a.click()
    document.body.removeChild(a)
}

const uploadFiles = (guildId: string, files: FileList | null) => {
    if (!files) return

    const formData = new FormData()

    for (let i = 0; i < files.length; i++) {
        const file = files[i]
        formData.append(file.name, file, file.name)
    }

    return fetch(`/api/${guildId}/sounds`, {
        method: 'POST',
        body: formData
    })
}

interface IDeleteDialogProps {
    open: boolean
    audioFile: IAudioFile | null
    onCancel: () => void
    onOk: () => void
}

const DeleteDialog: React.FC<IDeleteDialogProps> = (props) => {
    const { open, audioFile, onCancel, onOk } = props
    return (
        <Dialog
            open={open}
        >
            <DialogTitle>{`Delete ${audioFile?.name}`}</DialogTitle>
            <DialogContent>
                <DialogContentText>
                    {`Are you sure you want to delete sound '${audioFile?.name}'?`}
                </DialogContentText>
            </DialogContent>
            <DialogActions>
                <Button variant='outlined' onClick={onCancel} color='secondary'>
                    Cancel
                </Button>
                <Button variant='contained' onClick={onOk} color='warning' autoFocus>
                    Delete
                </Button>
            </DialogActions>
        </Dialog>
    )
}

interface IEditDialogProps {
    open: boolean
    audioFile: IAudioFile | null
    tags: string[]
    onCancel: () => void
    onOk: (values: EditValues) => void
}

type EditValues = Pick<IAudioFile, 'name' | 'tags'>

const EditDialog: React.FC<IEditDialogProps> = (props) => {
    const { open, audioFile, tags, onCancel, onOk } = props

    const [values, setValues] = useState<EditValues | null>(null)

    return (
        <Dialog
            fullWidth
            maxWidth='sm'
            open={open}
            TransitionProps={{
                onEnter: () => {
                    audioFile && setValues({ name: audioFile.name, tags: audioFile.tags })
                },
                onExited: () => {
                    setValues(null)
                }
            }}
        >
            <DialogTitle>{`Edit sound '${audioFile?.name}'`}</DialogTitle>
            <DialogContent>
                <TextField
                    size='small'
                    label='Name'
                    error={!values?.name.length}
                    value={values?.name || ''}
                    onChange={(event) => values && setValues({ ...values, name: event.target.value })}
                    variant='outlined'
                    margin='normal'
                />
                <Autocomplete<string, true>
                    multiple
                    size='small'
                    options={tags}
                    value={values?.tags || []}
                    onChange={(_event, tags) => values && setValues({ ...values, tags })}
                    renderInput={(params) => (
                        <TextField
                            {...params}
                            label='Tags'
                            variant='outlined'
                        />
                    )}
                />
            </DialogContent>
            <DialogActions>
                <Button variant='outlined' onClick={onCancel} color='secondary'>
                    Cancel
                </Button>
                <Button variant='contained' onClick={() => values && onOk(values)} color='primary' autoFocus>
                    Update
                </Button>
            </DialogActions>
        </Dialog>)
}

const Admin: React.FC = () => {

    const sounds = useSelector((state) => state.sounds.sounds)

    const guild = useSelector(selectedGuild)
    const guildMembers = useSelector(selectedGuildMembers)

    const dispatch = useDispatch()

    const { enqueueSnackbar } = useSnackbar()

    useEffect(() => {
        guild && dispatch(fetchSounds(guild.id))
        guild && dispatch(fetchGuildMembers(guild.id))
    }, [guild])

    const [deleteModalVisible, setDeleteModalVisible] = useState(false)
    const [editModalVisible, setEditModalVisible] = useState(false)
    const [selectedAudioFile, setSelectedAudioFile] = useState<IAudioFile | null>(null)

    const onEditAudioFile = (audioFile: IAudioFile) => {
        setSelectedAudioFile(audioFile)
        setEditModalVisible(true)
    }

    const onDeleteAudioFile = (audioFile: IAudioFile) => {
        setSelectedAudioFile(audioFile)
        setDeleteModalVisible(true)
    }

    if (!guild) return null
    if (!guild.isAdmin) return <Navigate to='/' replace />

    const membersById = R.indexBy(R.prop('id'), guildMembers || [])
    const tags = useMemo(() => getTags(sounds), [sounds])

    return (
        <>
            <DeleteDialog
                open={deleteModalVisible}
                audioFile={selectedAudioFile}
                onCancel={() => setDeleteModalVisible(false)}
                onOk={() => {
                    selectedAudioFile && dispatch(deleteSound(selectedAudioFile.guildId, selectedAudioFile.name))
                        .then(() => setDeleteModalVisible(false))
                }}
            />
            <EditDialog
                open={editModalVisible}
                audioFile={selectedAudioFile}
                tags={tags}
                onCancel={() => setEditModalVisible(false)}
                onOk={({ name, tags }) => {
                    if (!selectedAudioFile) {
                        return
                    }
                    const updated = { ...selectedAudioFile, name, tags }
                    dispatch(updateSound(selectedAudioFile.guildId, selectedAudioFile.name, updated))
                        .then(() => setEditModalVisible(false))
                }}
            />
            <Paper>
                <SoundsTable
                    rows={sounds}
                    members={membersById}
                    onUploadSounds={(files) => {
                        uploadFiles(guild.id, files)
                            ?.then(() => enqueueSnackbar(`Successfully uploaded ${files.length} sounds`, { variant: 'success' }))
                            .then(() => dispatch(fetchSounds(guild.id)))
                            .catch((err) => enqueueSnackbar('Failed to upload files: ' + err.message, { variant: 'error' }))
                    }}
                    onDownloadSounds={() => downloadZip(guild.id)}
                    onPlaySound={(audioFile) => dispatch(playSound(audioFile.guildId, audioFile.name))}
                    onDownloadSound={(audioFile) => downloadSound(audioFile.guildId, audioFile.name)}
                    onEditSound={onEditAudioFile}
                    onDeleteSound={onDeleteAudioFile}
                />
            </Paper>
        </>
    )
}

export default Admin