import React, { useEffect, useMemo, useState } from 'react'
import { message, Alert, Table, Divider, Modal, Button, Form, Input, Select, Upload } from 'antd'
import { DateTime } from 'luxon'
import * as R from 'ramda'
import { PlayCircleTwoTone, PlusOutlined, DownloadOutlined, EditTwoTone, DeleteTwoTone, WarningOutlined } from '@ant-design/icons'

import { useDispatch, useSelector } from '@/util'
import { fetchSounds, deleteSound, playSound, updateSound } from '@/actions/sounds'
import { IAudioFile, IGuildMember, AppDispatch, IGuild } from '@/types'
import { ColumnsType } from 'antd/lib/table'
import { fetchGuildMembers } from '@/actions/user'
import { UploadFile } from 'antd/lib/upload/interface'
import { selectedGuild, selectedGuildMembers } from '@/selectors/user'

const getTags = R.pipe<IAudioFile[], string[], string[], string[]>(
    R.chain<IAudioFile, string>(R.prop('tags')),
    R.uniq,
    R.invoker(0, 'sort')
)

const buildColumns = (dispatch: AppDispatch, onEditAudioFile: (audioFile: IAudioFile) => void, guild: IGuild, guildMembers: IGuildMember[]): ColumnsType<IAudioFile> => {

    const membersById = R.indexBy(R.prop('id'), guildMembers)
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
    }, {
        title: () =>
            <div style={{ float: 'right' }}>
                <Upload
                    action={`/api/${guild.id}/sounds`}
                    accept={'.mp3,.wav,.ogg'}
                    multiple
                    showUploadList={false}
                    onChange={({ file, fileList }) => {
                        if (file.status == 'error') {
                            message.error(`Failed to upload ${file.name}: ${file.response}`)
                        }
                        if (R.all<UploadFile>((file) => file.status !== 'uploading', fileList)) {
                            message.success(`Finished uploading ${fileList.length} file(s)`)
                            dispatch(fetchSounds(guild.id))
                        }
                    }}
                >
                    <Button
                        size='small'
                        title='Upload files'
                        type='primary'
                        shape='circle'
                        icon={<PlusOutlined style={{ fontSize: 14 }} />}
                    />
                </Upload>
                <Button
                    size='small'
                    style={{ marginLeft: 8 }}
                    title='Download audio files as zip'
                    shape='circle'
                    icon={<DownloadOutlined style={{ fontSize: 14 }} />}
                    href={`/api/${guild.id}/sounds/export`}
                />
            </div>,
        key: 'actions',
        render: (_text, audioFile) => <>
            <PlayCircleTwoTone
                title='Play audio'
                onClick={() => dispatch(playSound(guild.id, audioFile.name))}
            />
            <Divider type='vertical' />
            <a download href={`/api/${guild.id}/sounds/${audioFile.name}/download`}>
                <DownloadOutlined
                    title='Download'
                />
            </a>
            <Divider type='vertical' />
            <EditTwoTone
                title='Edit'
                onClick={() => onEditAudioFile(audioFile)}
            />
            <Divider type='vertical' />
            <DeleteTwoTone
                onClick={() => Modal.confirm({
                    icon: <WarningOutlined />,
                    title: `Delete ${audioFile.name}`,
                    content: `Are you sure you want to delete audio file "${audioFile.name}"?`,
                    onOk: () => dispatch(deleteSound(guild.id, audioFile.name))
                })}
                title='Delete'
            />
        </>
    }]
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
    const tags = useMemo(() => getTags(sounds), [sounds])

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
            <Table<IAudioFile> columns={columns} dataSource={sounds} rowKey='name' size='small' pagination={{ defaultPageSize: 20 }} loading={guildsLoading || soundsLoading || guildMembersLoading} />
        </>
    )
}

export default Admin