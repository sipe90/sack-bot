import React, { useEffect, useMemo, useState } from 'react'
import { Form, Input, Select } from 'antd'
import { DateTime } from 'luxon'
import * as R from 'ramda'
import filesize from 'filesize.js'
import { Button, Dialog, DialogActions, DialogContent, DialogContentText, DialogTitle } from '@material-ui/core'
import PublishIcon from '@material-ui/icons/Publish'
import GetAppIcon from '@material-ui/icons/GetApp'
import PlayArrowIcon from '@material-ui/icons/PlayArrow'
import EditIcon from '@material-ui/icons/Edit'
import DeleteIcon from '@material-ui/icons/Delete'
import MaterialTable, { Action, Column } from 'material-table'

import { useDispatch, useSelector } from '@/util'
import { fetchSounds, deleteSound, playSound, updateSound } from '@/actions/sounds'
import { IAudioFile } from '@/types'

import { fetchGuildMembers } from '@/actions/user'
import { selectedGuild, selectedGuildMembers } from '@/selectors/user'
import { useSnackbar } from 'notistack'
import { Redirect } from 'react-router-dom'

const getTags = R.pipe<IAudioFile[], string[], string[], string[]>(
    R.chain<IAudioFile, string>(R.prop('tags')),
    R.uniq,
    R.invoker(0, 'sort')
)

const downloadZip = (guildId: string) => {
    const a = document.createElement('a')
    a.href = `/api/${guildId}/sounds/export`
    document.body.appendChild(a)
    a.click()
    document.body.removeChild(a)
}

const downloadFile = (guildId: string, name: string) => {
    const a = document.createElement('a')
    a.href = `/api/${guildId}/sounds/${name}/download`
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

    useEffect(() => {
        guild && dispatch(fetchSounds(guild.id))
        guild && dispatch(fetchGuildMembers(guild.id))
    }, [guild])

    const [deleteModalVisible, setDeleteModalVisible] = useState(false)
    const [editModalVisible, setEditModalVisible] = useState(false)
    const [selectedAudioFile, setSelectedAudioFile] = useState<IAudioFile | null>(null)

    const [form] = Form.useForm()

    const { enqueueSnackbar } = useSnackbar()

    const onEditAudioFile = (audioFile: IAudioFile) => {
        setSelectedAudioFile(audioFile)
        form.setFieldsValue(audioFile)
        setEditModalVisible(true)
    }

    const onDeleteAudioFile = (audioFile: IAudioFile) => {
        setSelectedAudioFile(audioFile)
        setDeleteModalVisible(true)
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
                            .catch(() => enqueueSnackbar('Failed to upload files', { variant: 'error' }))
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
                downloadFile(audioFile.guildId, audioFile.name)
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
                    <Button onClick={async () => {
                        selectedAudioFile && await dispatch(deleteSound(selectedAudioFile.guildId, selectedAudioFile.name))
                        setDeleteModalVisible(false)
                    }} color="primary" autoFocus>
                        Update
                    </Button>
                </DialogActions>
            </Dialog>
            <Dialog
                fullWidth
                maxWidth='sm'
                open={editModalVisible}
            >
                <DialogTitle>{`Edit sound '${selectedAudioFile?.name}'`}</DialogTitle>
                <DialogContent>
                    <Form
                        form={form}
                        labelCol={{ span: 6 }}
                        wrapperCol={{ span: 18 }}
                    >
                        <Form.Item
                            label='Name'
                            name='name'
                            rules={[{ required: true, message: 'Name is required' }]}
                        >
                            <Input />
                        </Form.Item>
                        <Form.Item
                            label='Tags'
                            name='tags'
                        >
                            <Select
                                mode='tags'
                            >
                                {tags.map((tag) => <Select.Option key={tag} value={tag}>{tag}</Select.Option>)}
                            </Select>
                        </Form.Item>
                    </Form>
                </DialogContent>
                <DialogActions>
                    <Button onClick={() => setEditModalVisible(false)} color="secondary">
                        Cancel
                    </Button>
                    <Button onClick={() => {
                        if (!selectedAudioFile) {
                            return
                        }
                        form
                            .validateFields()
                            .then(({ name, tags }) => dispatch(updateSound(selectedAudioFile.guildId, selectedAudioFile.name, { ...selectedAudioFile, name, tags })))
                            .then(() => setEditModalVisible(false))
                            .catch(() => undefined)
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