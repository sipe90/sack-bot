import React, { useState } from 'react'
import * as R from 'ramda'
import filesize from 'filesize.js'
import Box from '@mui/material/Box'
import Table from '@mui/material/Table'
import TableBody from '@mui/material/TableBody'
import TableCell from '@mui/material/TableCell'
import TableContainer from '@mui/material/TableContainer'
import TableHead from '@mui/material/TableHead'
import TablePagination from '@mui/material/TablePagination'
import TableRow from '@mui/material/TableRow'
import TableSortLabel from '@mui/material/TableSortLabel'
import Toolbar from '@mui/material/Toolbar'
import Paper from '@mui/material/Paper'
import Typography from '@mui/material/Typography'
import Tooltip from '@mui/material/Tooltip'
import IconButton from '@mui/material/IconButton'
import GetAppIcon from '@mui/icons-material/GetApp'
import PublishIcon from '@mui/icons-material/Publish'
import PlayArrowIcon from '@mui/icons-material/PlayArrow'
import EditIcon from '@mui/icons-material/Edit'
import DeleteIcon from '@mui/icons-material/Delete'

import { IAudioFile, IDictionary, IGuildMember } from '@/types'
import { DateTime } from 'luxon'
import { Divider } from '@mui/material'


type Order = 'asc' | 'desc'

type SortProp = keyof Pick<IAudioFile, 'name' | 'extension' | 'size' | 'created' | 'createdBy' | 'modified' | 'modifiedBy'>

const getComparator = (order: Order, orderBy: SortProp) => order === 'desc'
    ? R.descend<IAudioFile>(R.propOr(Number.MAX_VALUE, orderBy))
    : R.ascend<IAudioFile>(R.propOr(Number.MAX_VALUE, orderBy))

interface IColumn {
    key: SortProp
    label: string
    numeric: boolean
    format?: (val: any) => any
    orderFormatted?: boolean
}

interface ISoundsTableToolbarProps {
    onUploadSounds: (files: FileList) => void
    onDownloadSounds: () => void
}

const SoundsTableToolbar: React.FC<ISoundsTableToolbarProps> = (props) => {
    const { onUploadSounds, onDownloadSounds } = props
    return (
        <Toolbar
            sx={{
                bgcolor: 'background.paper',
                pl: { sm: 2 },
                pr: { xs: 1, sm: 1 }
            }}
        >
            <Typography
                sx={{ flex: '1 1 100%' }}
                variant='h6'
                id='table-title'
                component='div'
            >
                Sounds
            </Typography>
            <input
                accept='.wav,.mp3,.ogg'
                style={{ display: 'none' }}
                id='icon-button-file'
                type='file'
                multiple
                onChange={(e) => {
                    const files = e.target.files
                    files && onUploadSounds(files)
                }}
            />
            <Tooltip title='Upload new sounds'>
                <IconButton
                    sx={{ mr: 1 }}
                >
                    <label htmlFor='icon-button-file' style={{ display: 'flex' }}>
                        <PublishIcon color='secondary' />
                    </label>
                </IconButton>
            </Tooltip>
            <Tooltip title='Download all sounds'>
                <IconButton
                    sx={{ mr: 1 }}
                    onClick={onDownloadSounds}
                >
                    <GetAppIcon color='secondary' />
                </IconButton>
            </Tooltip>
        </Toolbar>
    )
}

interface ISoundsTableHeaderProps {
    columns: readonly IColumn[]
    order: Order
    orderBy: string
    onSort: (column: IColumn) => void
}

const SoundsTableHeader: React.FC<ISoundsTableHeaderProps> = ({ columns, order, orderBy, onSort }) => (
    <TableHead >
        <TableRow >
            {columns.map((column) => (
                <TableCell
                    key={column.key}
                    align={column.numeric ? 'right' : 'left'}
                    sortDirection={orderBy === column.key ? order : false}
                    sx={{ bgcolor: 'background.paper' }}
                >
                    <TableSortLabel
                        active={orderBy === column.key}
                        direction={orderBy === column.key ? order : 'asc'}
                        onClick={() => onSort(column)}
                    >
                        {column.label}
                    </TableSortLabel>
                </TableCell>
            ))}
            <TableCell sx={{ bgcolor: 'background.paper' }} align='center'>
                Actions
            </TableCell>
        </TableRow>
    </TableHead>
)

interface ISoundsTableProps {
    rows: IAudioFile[]
    members: IDictionary<IGuildMember>
    onUploadSounds: (files: FileList) => void
    onDownloadSounds: () => void
    onPlaySound: (audioFile: IAudioFile) => void
    onDownloadSound: (audioFile: IAudioFile) => void
    onEditSound: (audioFile: IAudioFile) => void
    onDeleteSound: (audioFile: IAudioFile) => void
}

const SoundsTable: React.FC<ISoundsTableProps> = (props) => {
    const { rows, members, onUploadSounds, onDownloadSounds, onPlaySound, onDownloadSound, onEditSound, onDeleteSound } = props

    const [order, setOrder] = useState<Order>('asc')
    const [orderBy, setOrderBy] = useState<SortProp>('name')
    const [page, setPage] = React.useState(0)
    const [rowsPerPage, setRowsPerPage] = React.useState(50)

    const getUsername = (userId: string | null) => userId ? R.pathOr('-', [userId, 'name'], members) : ''

    const columns: readonly IColumn[] = [
        {
            key: 'name',
            label: 'Name',
            numeric: false
        },
        {
            key: 'extension',
            label: 'Ext',
            numeric: false
        },
        {
            key: 'size',
            label: 'Size',
            numeric: false,
            format: filesize
        },
        {
            key: 'created',
            label: 'Created',
            numeric: false,
            format: (ts) => ts ? DateTime.fromMillis(ts).toLocaleString(DateTime.DATETIME_SHORT) : '-'
        },
        {
            key: 'createdBy',
            label: 'Created by',
            numeric: false,
            format: getUsername,
            orderFormatted: true
        },
        {
            key: 'modified',
            label: 'Modified',
            numeric: false,
            format: (ts) => ts ? DateTime.fromMillis(ts).toLocaleString(DateTime.DATETIME_SHORT) : '-'
        },
        {
            key: 'modifiedBy',
            label: 'Modified by',
            numeric: false,
            format: getUsername,
            orderFormatted: true
        }
    ]

    const handleSort = (column: IColumn) => {
        const isAsc = orderBy === column.key && order === 'asc'
        setOrder(isAsc ? 'desc' : 'asc')
        setOrderBy(column.key)
    }

    const handleChangePage = (_event: unknown, newPage: number) => {
        setPage(newPage)
    }

    const handleChangeRowsPerPage = (event: React.ChangeEvent<HTMLInputElement>) => {
        setRowsPerPage(parseInt(event.target.value, 10))
        setPage(0)
    }

    // Avoid a layout jump when reaching the last page with empty rows.
    const emptyRows = page > 0 ? Math.max(0, (1 + page) * rowsPerPage - rows.length) : 0

    return (
        <Paper sx={{ width: '100%', mb: 2 }}>
            <SoundsTableToolbar
                onUploadSounds={onUploadSounds}
                onDownloadSounds={onDownloadSounds}
            />
            <TableContainer sx={{ maxHeight: 870 }} >
                <Table
                    stickyHeader
                    size='small'
                >
                    <SoundsTableHeader
                        columns={columns}
                        order={order}
                        orderBy={orderBy}
                        onSort={handleSort}
                    />
                    <TableBody>
                        {rows.slice().sort(getComparator(order, orderBy))
                            .slice(page * rowsPerPage, page * rowsPerPage + rowsPerPage)
                            .map((row, idx) => {
                                return (
                                    <TableRow
                                        id={String(idx)}
                                        hover
                                        tabIndex={-1}
                                        key={row.name}
                                    >
                                        {columns.map((column, idx) => {
                                            const v = row[column.key]
                                            const val = column.format ? column.format(v) : v
                                            return (<TableCell key={String(idx)}>{val}</TableCell>)
                                        })}
                                        <TableCell>
                                            <Box
                                                sx={{
                                                    display: 'flex',
                                                    alignItems: 'center',
                                                    width: 'fit-content'
                                                }}
                                            >
                                                <Tooltip title='Play sound'>
                                                    <IconButton
                                                        sx={{ p: 0.5, mr: 1 }}
                                                        onClick={() => onPlaySound(row)}
                                                    >
                                                        <PlayArrowIcon color='secondary' fontSize='small' />
                                                    </IconButton>
                                                </Tooltip>
                                                <Divider flexItem orientation='vertical' />
                                                <Tooltip title='Download sound'>
                                                    <IconButton
                                                        sx={{ p: 0.5, mx: 1 }}
                                                        onClick={() => onDownloadSound(row)}
                                                    >
                                                        <GetAppIcon color='secondary' fontSize='small' />
                                                    </IconButton>
                                                </Tooltip>
                                                <Divider flexItem orientation='vertical' />
                                                <Tooltip title='Edit sound'>
                                                    <IconButton
                                                        sx={{ p: 0.5, mx: 1 }}
                                                        onClick={() => onEditSound(row)}
                                                    >
                                                        <EditIcon color='secondary' fontSize='small' />
                                                    </IconButton>
                                                </Tooltip>
                                                <Divider flexItem orientation='vertical' />
                                                <Tooltip title='Delete sound'>
                                                    <IconButton
                                                        sx={{ p: 0.5, ml: 1 }}
                                                        onClick={() => onDeleteSound(row)}
                                                    >
                                                        <DeleteIcon color='secondary' fontSize='small' />
                                                    </IconButton>
                                                </Tooltip>
                                            </Box>
                                        </TableCell>
                                    </TableRow>
                                )
                            })}
                        {emptyRows > 0 && (
                            <TableRow
                                style={{
                                    height: 33 * emptyRows,
                                }}
                            >
                                <TableCell colSpan={columns.length + 1} />
                            </TableRow>
                        )}
                    </TableBody>
                </Table>
            </TableContainer>
            <TablePagination
                rowsPerPageOptions={[10, 25, 50, 100]}
                component='div'
                count={rows.length}
                rowsPerPage={rowsPerPage}
                page={page}
                onPageChange={handleChangePage}
                onRowsPerPageChange={handleChangeRowsPerPage}
                sx={{ bgcolor: 'background.paper' }}
            />
        </Paper>
    )
}

export default SoundsTable