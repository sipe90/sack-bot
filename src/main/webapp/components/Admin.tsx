import React, { useEffect, useMemo, useState } from 'react'
import { message, Alert, Table, Divider, Modal, Button, Form, Input, Select, Upload } from 'antd'
import { DateTime } from 'luxon'
import * as R from 'ramda'
import { PlayCircleTwoTone, PlusOutlined, DownloadOutlined, EditTwoTone, DeleteTwoTone, WarningOutlined } from '@ant-design/icons'

import { useDispatch, useSelector } from '@/util'
import { fetchSounds, deleteSound, playSound, updateSound } from '@/actions/sounds'
import { IAudioFile, IGuildMember, AppDispatch } from '@/types'
import { ColumnsType } from 'antd/lib/table'
import { fetchGuildMembers } from '@/actions/user'
import { UploadFile } from 'antd/lib/upload/interface'

const getTags = R.pipe<IAudioFile[], string[], string[], string[]>(
    R.chain<IAudioFile, string>(R.prop('tags')),
    R.uniq,
    R.invoker(0, 'sort')
)

const buildColumns = (dispatch: AppDispatch, onEditAudioFile: (audioFile: IAudioFile) => void, guildId: string | null, guildMembers: { [guildId: string]: IGuildMember[]}): ColumnsType<IAudioFile> => {

    const members = guildId ? guildMembers[guildId] || [] : []
    const membersById = R.indexBy(R.prop('id'), members)
    const getUsername = (userId: string | null) => userId ? R.pathOr('Unknown', [userId, 'name'], membersById) : ''

    return [{
        title: 'Name',
        dataIndex: 'name',
        filtered: true,
        sorter: (a, b) => a.name.localeCompare(b.name)
    }, {
        title: 'Extension',
        dataIndex: 'extension',
        sorter: (a, b) => a.name.localeCompare(b.name)
    }, {
        title: 'Size',
        dataIndex: 'size',
        render: (size: number) => `${size / 1000} KB`,
        sorter: (a, b) => a.size - b.size
    }, { 
        title: 'Created',
        dataIndex: 'created',
        render: (instant: number) => DateTime.fromMillis(instant).toLocaleString(DateTime.DATETIME_SHORT_WITH_SECONDS),
        sorter: (a, b) => a.created - b.created
    }, {
        title: 'Created by',
        dataIndex: 'createdBy',
        render: (userId: string) => userId ? getUsername(userId) : '',
        sorter: (a, b) => getUsername(a.createdBy).localeCompare(getUsername(b.createdBy))
    }, {
        title: 'Modified',
        dataIndex: 'modified',
        render: (instant: number) => instant ? DateTime.fromMillis(instant).toLocaleString(DateTime.DATETIME_SHORT_WITH_SECONDS) : '',
        sorter: (a, b) => (a.modified || 0) - (b.modified || 0)
    }, {
        title: 'Modified by',
        dataIndex: 'modifiedBy',
        render: (userId: string) => userId ? getUsername(userId) : '',
        sorter: (a, b) => getUsername(a.modifiedBy).localeCompare(getUsername(b.modifiedBy))
    },{
        title: () => 
            <div style={{ float: 'right'}}>
                <Upload
                    action={`/api/${guildId}/sounds`}
                    accept={'.mp3,.wav'}
                    multiple
                    showUploadList={false}
                    onChange={({ file, fileList }) => {
                        if (file.status == 'error') {
                            message.error(`Failed to upload ${file.name}: ${file.response}`)
                        }
                        if (R.all<UploadFile>((file) => file.status !== 'uploading', fileList)) {
                            message.success(`Finished uploading ${fileList.length} file(s)`)
                            guildId && dispatch(fetchSounds(guildId))
                        }
                    }}
                >
                    <Button
                        title='Upload files'
                        type='primary'
                        shape='circle'
                        icon={<PlusOutlined/>}
                    />
                </Upload>
                <Button
                    style={{ marginLeft: 8 }}
                    title='Download audio files as zip'
                    shape='circle'
                    icon={<DownloadOutlined/>}
                    href={`/api/${guildId}/sounds/export`}
                />
            </div>,
        key: 'actions',
        render: (_text, audioFile) => <>
                <PlayCircleTwoTone
                    title='Play audio'
                    onClick={() => guildId && dispatch(playSound(guildId, audioFile.name))}
                />
                <Divider type='vertical'/>
                <a download href={`/api/${guildId}/sounds/${audioFile.name}/download`}>
                    <DownloadOutlined
                        title='Download'
                    />
                </a>
                <Divider type='vertical'/>
                <EditTwoTone
                    title='Edit'
                    onClick={() => onEditAudioFile(audioFile)}
                />
                <Divider type='vertical'/>
                <DeleteTwoTone
                    onClick={() => Modal.confirm({
                        icon: <WarningOutlined />,
                        title: `Delete ${audioFile.name}`,
                        content: `Are you sure you want to delete audio file "${audioFile.name}"?`,
                        onOk: () => guildId ? dispatch(deleteSound(guildId, audioFile.name)) : undefined
                    })}
                    title='Delete'
                />
            </>
    }]
}

const Admin: React.FC = () => {

    const { 
        selectedGuild, 
        sounds,
        soundsLoading,
        guilds,
        guildsLoading,
        guildMembers,
        guildMembersLoading
    } = useSelector((state) => ({
        selectedGuild: state.user.selectedGuild,
        guilds: state.user.guilds,
        guildsLoading: state.user.guildsLoading,
        sounds: state.sounds.sounds,
        soundsLoading: state.sounds.soundsLoading,
        guildMembers: state.user.guildMembers,
        guildMembersLoading: state.user.guildMembersLoading
    }))

    const dispatch = useDispatch()

    useEffect(() => {
        selectedGuild && dispatch(fetchSounds(selectedGuild))
        selectedGuild && dispatch(fetchGuildMembers(selectedGuild))
    }, [selectedGuild])

    const [editModalVisible, setEditModalVisible] = useState(false)
    const [selectedAudioFile, setSelectedAudioFile] = useState<IAudioFile | null>(null)

    const [form] = Form.useForm()

    const onEditAudioFile = (audioFile: IAudioFile) => {
        setSelectedAudioFile(audioFile)
        form.setFieldsValue(audioFile)
        setEditModalVisible(true)
    }

    const columns = useMemo(() => buildColumns(dispatch, onEditAudioFile, selectedGuild, guildMembers), [guildMembers, selectedGuild])
    const tags = useMemo(() => getTags(sounds), [sounds])

    const guild = guilds.find(({ id }) => id === selectedGuild)

    if (!guild) return null

    if (!guild.isAdmin) return <Alert message="You have no power here!" type="error" showIcon />

    return (
        <>
            <Modal
                title={`Edit audio file "${selectedAudioFile?.name}"`}
                visible={editModalVisible}
                forceRender
                destroyOnClose={false}
                okText="Update"
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
                        label="Name"
                        name="name"
                        rules={[{ required: true, message: 'Name is required' }]}
                    >
                        <Input />
                    </Form.Item>
                    <Form.Item
                        label="Tags"
                        name="tags"
                    >
                        <Select 
                            mode="tags"
                        >
                            {tags.map((tag) => <Select.Option key={tag} value={tag}>{tag}</Select.Option>)}
                        </Select>
                    </Form.Item>
                </Form>
            </Modal>
            <Table<IAudioFile> columns={columns} dataSource={sounds} rowKey='name' size='small' pagination={false} loading={guildsLoading || soundsLoading || guildMembersLoading}/>
        </>
    )
}

export default Admin