import React, { ChangeEvent, useEffect, useMemo, useState } from 'react'
import { DateTime } from 'luxon'
import * as R from 'ramda'
import filesize from 'filesize.js'
import { Button, Dialog, DialogActions, DialogContent, DialogContentText, DialogTitle, TextField } from '@mui/material'
import Autocomplete from '@mui/lab/Autocomplete'
import PublishIcon from '@mui/icons-material/Publish'
import GetAppIcon from '@mui/icons-material/GetApp'
import PlayArrowIcon from '@mui/icons-material/PlayArrow'
import EditIcon from '@mui/icons-material/Edit'
import DeleteIcon from '@mui/icons-material/Delete'
import MaterialTable, { Action, Column } from 'material-table'

import { useDispatch, useSelector } from '@/util'
import { fetchSounds, deleteSound, playSound, updateSound } from '@/actions/sounds'
import { IAudioFile } from '@/types'

import { fetchGuildMembers } from '@/actions/user'
import { selectedGuild, selectedGuildMembers } from '@/selectors/user'
import { useSnackbar } from 'notistack'
import { Redirect } from 'react-router-dom'

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

type Actions<RowData extends object> = (Action<RowData> | ((rowData: RowData) => Action<RowData>))[]

const Admin: React.FC = () => {

    const sounds = useSelector((state) => state.sounds.sounds)
    const soundsLoading = useSelector((state) => state.sounds.soundsLoading)
    const guildsLoading = useSelector((state) => state.user.guildsLoading)
    const guildMembersLoading = useSelector((state) => state.user.guildMembersLoading)

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
    const [updateAudioFile, setUpdateAudioFile] = useState<Pick<IAudioFile, 'name' | 'tags'> | null>(null)

    const onEditAudioFile = (audioFile: IAudioFile) => {
        setSelectedAudioFile(audioFile)
        setUpdateAudioFile({ name: audioFile.name, tags: audioFile.tags })
        setEditModalVisible(true)
    }

    const onDeleteAudioFile = (audioFile: IAudioFile) => {
        setSelectedAudioFile({ ...audioFile })
        setDeleteModalVisible(true)
    }

    const onNameChange = (event: ChangeEvent<HTMLInputElement>) => {
        if (!updateAudioFile) return
        setUpdateAudioFile({ ...updateAudioFile, name: event.target.value })
    }

    const onTagsChange = (_event: ChangeEvent<{}>, tags: string[]) => {
        if (!updateAudioFile) return
        setUpdateAudioFile({ ...updateAudioFile, tags })
    }

    if (!guild) return null

    const columns = useMemo(() => {

        const membersById = R.indexBy(R.prop('id'), guildMembers || [])
        const getUsername = (userId: string | null) => userId ? R.pathOr('Unknown', [userId, 'name'], membersById) : ''

        const columns: Column<IAudioFile>[] = [
            {
                field: 'name',
                title: 'Name'
            },
            {
                field: 'extension',
                title: 'Ext',
                width: 80
            },
            {
                title: 'Size',
                render: ({ size }) => filesize(size)
            },
            {
                field: 'created',
                title: 'Created',
                type: 'datetime',
                width: 190,
                render: ({ created }) => created ? DateTime.fromMillis(created).toLocaleString(DateTime.DATETIME_SHORT_WITH_SECONDS) : ''
            },
            {
                title: 'Created by',
                render: ({ createdBy }) => getUsername(createdBy)
            },
            {
                field: 'modified',
                title: 'Modified',
                width: 190,
                render: ({ modified }) => modified ? DateTime.fromMillis(modified).toLocaleString(DateTime.DATETIME_SHORT_WITH_SECONDS) : ''
            },
            {
                title: 'Modified by',
                render: ({ modifiedBy }) => getUsername(modifiedBy)
            }
        ]

        return columns
    }, [guildMembers])

    const actions: Actions<IAudioFile> = useMemo(() => [
        {
            isFreeAction: true,
            icon: () => <>
                <input
                    accept='.wav,.mp3,.ogg'
                    style={{ display: 'none' }}
                    id='icon-button-file'
                    type='file'
                    multiple
                    onChange={(e) => {
                        const files = e.target.files
                        uploadFiles(guild.id, files)
                            ?.then(() => enqueueSnackbar(`Successfully uploaded ${files?.length} sounds`, { variant: 'success' }))
                            .then(() => dispatch(fetchSounds(guild.id)))
                            .catch((err) => enqueueSnackbar('Failed to upload files: ' + err.message, { variant: 'error' }))
                    }}
                />
                <label htmlFor='icon-button-file' style={{ display: 'flex' }}>
                    <PublishIcon color='primary' />
                </label>
            </>,
            onClick: () => { },
            tooltip: 'Upload sounds'
        },
        {
            isFreeAction: true,
            icon: () => <GetAppIcon color='primary' />,
            onClick: () => downloadZip(guild.id),
            tooltip: 'Download all sounds'
        },
        {
            icon: () => <PlayArrowIcon color='action' fontSize='small' />,
            onClick: (_e, data) => {
                const audioFile = data as IAudioFile
                dispatch(playSound(audioFile.guildId, audioFile.name))
            },
            tooltip: 'Play audio'
        },
        {
            icon: () => <GetAppIcon color='action' fontSize='small' />,
            onClick: (_e, data) => {
                const audioFile = data as IAudioFile
                downloadSound(audioFile.guildId, audioFile.name)
            },
            tooltip: 'Download'
        },
        {
            icon: () => <EditIcon color='action' fontSize='small' />,
            tooltip: 'Edit',
            onClick: (_e, data) => onEditAudioFile(data as IAudioFile)
        },
        {
            icon: () => <DeleteIcon color='action' fontSize='small' />,
            onClick: (_e, data) => onDeleteAudioFile(data as IAudioFile),
            tooltip: 'Delete'
        }
    ], [dispatch, onEditAudioFile, guild])

    const tags = useMemo(() => getTags(sounds), [sounds])

    if (!guild.isAdmin) return <Redirect to='/' />

    return (
        <>

            <Dialog
                open={deleteModalVisible}
            >
                <DialogTitle>{`Delete ${selectedAudioFile?.name}`}</DialogTitle>
                <DialogContent>
                    <DialogContentText>
                        {`Are you sure you want to delete sound '${selectedAudioFile?.name}'?`}
                    </DialogContentText>
                </DialogContent>
                <DialogActions>
                    <Button onClick={() => setDeleteModalVisible(false)} color="secondary">
                        Cancel
                    </Button>
                    <Button onClick={() => {
                        selectedAudioFile && dispatch(deleteSound(selectedAudioFile.guildId, selectedAudioFile.name))
                            .then(() => setDeleteModalVisible(false))
                    }} color="primary" autoFocus>
                        Delete
                    </Button>
                </DialogActions>
            </Dialog>
            <Dialog
                fullWidth
                maxWidth='sm'
                open={editModalVisible}
                TransitionProps={{
                    onExited: () => {
                        setSelectedAudioFile(null)
                        setUpdateAudioFile(null)
                    }
                }}

            >
                <DialogTitle>{`Edit sound '${selectedAudioFile?.name}'`}</DialogTitle>
                <DialogContent>
                    <TextField
                        size='small'
                        label='Name'
                        error={!updateAudioFile?.name.length}
                        value={updateAudioFile?.name}
                        onChange={onNameChange}
                        variant='outlined'
                        margin='normal'
                    />
                    <Autocomplete<string, true>
                        multiple
                        size='small'
                        options={tags}
                        value={updateAudioFile?.tags}
                        onChange={onTagsChange}
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
                    <Button onClick={() => {
                        setEditModalVisible(false)
                    }} color="secondary">
                        Cancel
                    </Button>
                    <Button onClick={() => {
                        if (!updateAudioFile || !selectedAudioFile) {
                            return
                        }
                        const updated = { ...selectedAudioFile, name: updateAudioFile.name, tags: updateAudioFile.tags }
                        dispatch(updateSound(selectedAudioFile.guildId, selectedAudioFile.name, updated))
                            .then(() => setEditModalVisible(false))
                    }} color="primary" autoFocus>
                        Update
                    </Button>
                </DialogActions>
            </Dialog>
            <MaterialTable<IAudioFile>
                title='Sounds'
                columns={columns}
                actions={actions}
                data={sounds}
                isLoading={guildsLoading || soundsLoading || guildMembersLoading}
                options={{
                    actionsColumnIndex: -1,
                    padding: 'dense',
                    pageSize: 10,
                    pageSizeOptions: [10, 25, 50, 100]
                }}
            />
        </>
    )
}

export default Admin