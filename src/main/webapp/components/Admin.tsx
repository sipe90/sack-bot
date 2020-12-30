import React, { useEffect, useMemo, useState } from 'react'
import { Alert, Modal, Form, Input, Select } from 'antd'
import { DateTime } from 'luxon'
import * as R from 'ramda'
import filesize from 'filesize.js'
import { WarningOutlined } from '@ant-design/icons'
import PublishIcon from '@material-ui/icons/Publish'
import GetAppIcon from '@material-ui/icons/GetApp'
import PlayArrowIcon from '@material-ui/icons/PlayArrow'
import EditIcon from '@material-ui/icons/Edit'
import DeleteIcon from '@material-ui/icons/Delete'
import MaterialTable, { Action, Column } from 'material-table'

import { useDispatch, useSelector } from '@/util'
import { fetchSounds, deleteSound, playSound, updateSound } from '@/actions/sounds'
import { IAudioFile, IGuildMember, AppDispatch } from '@/types'

import { fetchGuildMembers } from '@/actions/user'
import { selectedGuild, selectedGuildMembers } from '@/selectors/user'

const getTags = R.pipe<IAudioFile[], string[], string[], string[]>(
    R.chain<IAudioFile, string>(R.prop('tags')),
    R.uniq,
    R.invoker(0, 'sort')
)

const buildColumns = (guildMembers: IGuildMember[]): Column<IAudioFile>[] => {

    const membersById = R.indexBy(R.prop('id'), guildMembers)
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
}

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

type Actions<RowData extends object> = (Action<RowData> | ((rowData: RowData) => Action<RowData>))[]

const buildActions = (dispatch: AppDispatch, onEditAudioFile: (audioFile: IAudioFile) => void, guildId: string): Actions<IAudioFile> => [
    {
        isFreeAction: true,
        icon: () => <>
            <input accept='.wav,.mp3,.ogg' style={{ display: 'none' }} id='icon-button-file' type='file' multiple />
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
        onClick: () => downloadZip(guildId),
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
        onClick: (_e, data) => {
            const audioFile = data as IAudioFile
            Modal.confirm({
                icon: <WarningOutlined />,
                title: `Delete ${audioFile.name}`,
                content: `Are you sure you want to delete audio file '${audioFile.name}'?`,
                onOk: () => dispatch(deleteSound(audioFile.guildId, audioFile.name))
            })
        },
        tooltip: 'Delete'
    }
]

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

    const columns = useMemo(() => buildColumns(guildMembers || []), [guildMembers])
    const actions = useMemo(() => buildActions(dispatch, onEditAudioFile, guild.id), [dispatch, onEditAudioFile, guild])
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