import React, { useEffect, useMemo } from 'react'
import { Alert, Table, Divider, Modal, Button } from 'antd'
import { DateTime } from 'luxon'
import * as R from 'ramda'
import { PlayCircleTwoTone, DownloadOutlined, EditTwoTone, DeleteTwoTone, WarningOutlined } from '@ant-design/icons'

import { useDispatch, useSelector } from '@/util'
import { fetchSounds, deleteSound, playSound } from '@/actions/sounds'
import { IAudioFile, IGuildMember, AppDispatch } from '@/types'
import { ColumnsType } from 'antd/lib/table'
import { fetchGuildMembers } from '@/actions/user'

const buildColumns = (dispatch: AppDispatch, guildId: string | null, guildMembers: { [guildId: string]: IGuildMember[]}): ColumnsType<IAudioFile> => {

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
            <Button
                style={{ float: 'right'}}
                title='Download audio files as zip'
                type='primary'
                shape='round'
                icon={<DownloadOutlined/>}
                href={`/api/${guildId}/sounds/export`}
            >
                Export
            </Button>,
        key: 'actions',
        render: (_text, { name }) => <>
                <PlayCircleTwoTone
                    title='Play audio'
                    onClick={() => guildId && dispatch(playSound(guildId, name))}
                />
                <Divider type='vertical'/>
                <a download href={`/api/${guildId}/sounds/${name}/download`}>
                    <DownloadOutlined
                        title='Download'
                    />
                </a>
                <Divider type='vertical'/>
                <EditTwoTone
                    title='Edit'
                />
                <Divider type='vertical'/>
                <DeleteTwoTone
                    onClick={() => Modal.confirm({
                        icon: <WarningOutlined />,
                        title: `Delete ${name}`,
                        content: `Are you sure you want to delete audio file "${name}"?`,
                        onOk: () => guildId ? dispatch(deleteSound(guildId, name)) : undefined
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

    const guild = guilds.find(({ id }) => id === selectedGuild)

    const columns = useMemo(() => buildColumns(dispatch, selectedGuild, guildMembers), [guildMembers, selectedGuild])

    if (!guild) return null

    return guild.isAdmin ? (
        <Table<IAudioFile> columns={columns} dataSource={sounds} rowKey='name' size='small' pagination={false} loading={guildsLoading ||soundsLoading || guildMembersLoading}/>
    ) : <Alert message="You have no power here!" type="error" showIcon />
}

export default Admin