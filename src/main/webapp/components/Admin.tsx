import React, { useEffect, useMemo, useState } from 'react'
import { Alert, Modal, Form, Input, Select } from 'antd'
import { DateTime } from 'luxon'
import * as R from 'ramda'
import filesize from 'filesize.js'
import { WarningOutlined } from '@ant-design/icons'
import { DataGrid, ColDef } from '@material-ui/data-grid'
import PublishIcon from '@material-ui/icons/Publish'
import GetAppIcon from '@material-ui/icons/GetApp'
import PlayArrowIcon from '@material-ui/icons/PlayArrow'
import EditIcon from '@material-ui/icons/Edit'
import DeleteIcon from '@material-ui/icons/Delete'

import { useDispatch, useSelector } from '@/util'
import { fetchSounds, deleteSound, playSound, updateSound } from '@/actions/sounds'
import { IAudioFile, IGuildMember, AppDispatch, IGuild } from '@/types'

import { fetchGuildMembers } from '@/actions/user'
import { selectedGuild, selectedGuildMembers } from '@/selectors/user'
import { Box, Divider, IconButton } from '@material-ui/core'

const getTags = R.pipe<IAudioFile[], string[], string[], string[]>(
    R.chain<IAudioFile, string>(R.prop('tags')),
    R.uniq,
    R.invoker(0, 'sort')
)

const buildColumns = (dispatch: AppDispatch, onEditAudioFile: (audioFile: IAudioFile) => void, guild: IGuild, guildMembers: IGuildMember[]): ColDef[] => {

    const membersById = R.indexBy(R.prop('id'), guildMembers)
    const getUsername = (userId: string | null) => userId ? R.pathOr('Unknown', [userId, 'name'], membersById) : ''

    const VDivider = () => <Divider orientation='vertical' style={{ height: 18, marginLeft: 2, marginRight: 2 }} />

    const columns: ColDef[] = [
        {
            field: 'name',
            headerName: 'Name',
            flex: 1
        },
        {
            field: 'extension',
            headerName: 'Ext',
            width: 80
        },
        {
            field: 'size',
            headerName: 'Size',
            type: 'number',
            valueFormatter: ({ value }) => filesize(value as number)
        },
        {
            field: 'created',
            headerName: 'Created',
            width: 190,
            valueFormatter: ({ value }) => value ? DateTime.fromMillis(value as number).toLocaleString(DateTime.DATETIME_SHORT_WITH_SECONDS) : ''
        },
        {
            field: 'createdBy',
            headerName: 'Created by',
            valueGetter: ({ value }) => value ? getUsername(value as string) : ''
        },
        {
            field: 'modified',
            headerName: 'Modified',
            width: 190,
            valueFormatter: ({ value }) => value ? DateTime.fromMillis(value as number).toLocaleString(DateTime.DATETIME_SHORT_WITH_SECONDS) : ''
        },
        {
            field: 'modifiedBy',
            headerName: 'Modified by',
            valueGetter: ({ value }) => value ? getUsername(value as string) : ''
        },
        {
            field: 'actions',
            sortable: false,
            width: 160,
            renderHeader: () => (
                <Box display='flex' alignItems='center'>
                    <div>
                        <input accept='.wav,.mp3,.ogg' style={{ display: 'none' }} id='icon-button-file' type='file' />
                        <label htmlFor='icon-button-file'>
                            <IconButton
                                size='small'
                                color='primary'
                                title='Upload files'
                                aria-label='upload'
                                component='span'
                            >
                                <PublishIcon />
                            </IconButton>
                        </label>
                    </div>
                    <VDivider />
                    <div>
                        <IconButton
                            href={`/api/${guild.id}/sounds/export`}
                            size='small'
                            title='Download audio files as zip'
                            color='primary'
                            aria-label='download files'
                            component='a'
                        >
                            <GetAppIcon />
                        </IconButton>
                    </div>
                </Box>
            ),
            renderCell: ({ data: audioFile }) => (
                <>
                    <IconButton
                        size='small'
                        color='primary'
                        aria-label='play'
                        title='Play audio'
                        onClick={() => dispatch(playSound(guild.id, audioFile.name))}
                    >
                        <PlayArrowIcon />
                    </IconButton>
                    <VDivider />
                    <IconButton
                        title='Download'
                        href={`/api/${guild.id}/sounds/${audioFile.name}/download`}
                        size='small'
                        color='primary'
                        aria-label='download files'
                        component='a'
                    >
                        <GetAppIcon />
                    </IconButton>
                    <VDivider />
                    <IconButton
                        size='small'
                        color='primary'
                        aria-label='edit'
                        title='Edit'
                        onClick={() => onEditAudioFile(audioFile as unknown as IAudioFile)}
                    >
                        <EditIcon />
                    </IconButton>
                    <VDivider />
                    <IconButton
                        size='small'
                        color='primary'
                        aria-label='delete'
                        title='Delete'
                        onClick={() => Modal.confirm({
                            icon: <WarningOutlined />,
                            title: `Delete ${audioFile.name}`,
                            content: `Are you sure you want to delete audio file '${audioFile.name}'?`,
                            onOk: () => dispatch(deleteSound(guild.id, audioFile.name))
                        })}
                    >
                        <DeleteIcon />
                    </IconButton>
                </>
            )
        }
    ]

    return columns
}

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

    const [editModalVisible, setEditModalVisible] = useState(false)
    const [selectedAudioFile, setSelectedAudioFile] = useState<IAudioFile | null>(null)

    const [form] = Form.useForm()

    const onEditAudioFile = (audioFile: IAudioFile) => {
        setSelectedAudioFile(audioFile)
        form.setFieldsValue(audioFile)
        setEditModalVisible(true)
    }

    if (!guild) return null

    const columns = useMemo(() => buildColumns(dispatch, onEditAudioFile, guild, guildMembers || []), [guildMembers, guild])
    const rows = sounds.map((s) => ({ ...s, id: s.name }))
    const tags = useMemo(() => getTags(sounds), [sounds])

    if (!guild.isAdmin) return <Alert message='You have no power here!' type='error' showIcon />

    return (
        <>
            <Modal
                title={`Edit audio file '${selectedAudioFile?.name}'`}
                visible={editModalVisible}
                forceRender
                destroyOnClose={false}
                okText='Update'
                onOk={() => {
                    if (!selectedAudioFile) {
                        return
                    }
                    form
                        .validateFields()
                        .then(({ name, tags }) => dispatch(updateSound(selectedAudioFile.guildId, selectedAudioFile.name, { ...selectedAudioFile, name, tags })))
                        .then(() => setEditModalVisible(false))
                        .catch(() => undefined)
                }}
                onCancel={() => setEditModalVisible(false)}
                afterClose={() => setSelectedAudioFile(null)}
            >
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
            </Modal>
            <DataGrid
                columns={columns}
                rows={rows}
                rowHeight={38}
                loading={guildsLoading || soundsLoading || guildMembersLoading}
                disableSelectionOnClick
            />
        </>
    )
}

export default Admin