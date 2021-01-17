import React, { useEffect, useCallback, useMemo, useState } from 'react'
import * as R from 'ramda'
import {
    Box,
    Button,
    CardActionArea,
    CircularProgress,
    FormControl,
    FormControlLabel,
    FormLabel,
    Grid,
    Input,
    makeStyles,
    Menu,
    MenuItem,
    Radio,
    RadioGroup,
    Select,
    Slider,
    TextField,
    Typography
} from '@material-ui/core'

import { useDispatch, useSelector } from '@/util'
import { fetchSounds, playSound, playRandomSound, playUrl } from '@/actions/sounds'
import { IAudioFile, IDictionary } from '@/types'
import { selectedGuildMembership } from '@/selectors/user'
import { updateEntrySound, updateExitSound } from '@/actions/user'
import Divider from '@/components/Divider'
import { useSnackbar } from 'notistack'

type GroupBy = 'alphabetic' | 'tag'

const isSubset = R.curry((xs: any[], ys: any[]) =>
    R.all(R.contains(R.__, ys), xs))

const filterSounds = (tagFilter: string[]) => (sounds: IAudioFile[]) => tagFilter.length ? sounds.filter((sound) => isSubset(tagFilter, sound.tags)) : sounds

const groupByFirstLetter = R.groupBy<IAudioFile>(
    R.pipe(
        R.pathOr('', ['name', 0]),
        R.toUpper
    )
)

const groupByTags = (sounds: IAudioFile[]) => sounds.reduce<IDictionary<IAudioFile[]>>((acc, val) => {
    val.tags.forEach((tag) => {
        acc[tag] = R.append(val, R.propOr([], tag, acc))
    })
    return acc
}, {})

const filterAndGroup = (tagFilter: string[], groupBy: GroupBy, sounds: IAudioFile[]) => R.pipe<IAudioFile[], IAudioFile[], IDictionary<IAudioFile[]>>(
    filterSounds(tagFilter),
    R.ifElse(
        R.always(R.equals('alphabetic', groupBy)),
        groupByFirstLetter,
        groupByTags
    )
)(sounds)

const getTags = R.pipe<IAudioFile[], string[], string[], string[]>(
    R.chain<IAudioFile, string>(R.prop('tags')),
    R.uniq,
    R.invoker(0, 'sort')
)

const useStyles = makeStyles((theme) => ({
    divider: {
        marginTop: theme.spacing(4),
        marginBottom: theme.spacing(4)
    },
    card: {
        width: 120,
        height: 32,
        borderRadius: 0,
        textAlign: 'center',
        overflow: 'hidden',
        textOverflow: 'ellipsis',
        padding: 8
    },
    loadingIndicator: {
        marginBottom: theme.spacing(2)
    }
}))

const defVolume = 75

const Soundboard: React.FC = () => {

    const classes = useStyles()

    const selectedGuild = useSelector((state => state.user.selectedGuildId))
    const soundsLoading = useSelector((state => state.sounds.soundsLoading))
    const sounds = useSelector((state => state.sounds.sounds))
    const membership = useSelector(selectedGuildMembership)

    const dispatch = useDispatch()

    const { enqueueSnackbar } = useSnackbar()

    const [volume, setVolume] = useState<number>(defVolume)
    const [url, setUrl] = useState<string>('')
    const [tagFilter, setTagFilter] = useState<string[]>([])
    const [groupBy, setGroupBy] = useState<GroupBy>('alphabetic')

    const onPlayRandomSound = useCallback(() => selectedGuild &&
        dispatch(playRandomSound(selectedGuild, volume, tagFilter))
            .catch((err) => enqueueSnackbar('Failed to play sound: ' + err.message, { variant: 'error' })), [selectedGuild, volume, tagFilter])
    const onPlaySound = useCallback((sound: string) => selectedGuild &&
        dispatch(playSound(selectedGuild, sound, volume))
            .catch((err) => enqueueSnackbar('Failed to play sound: ' + err.message, { variant: 'error' })), [selectedGuild, volume])
    const onPlayUrl = useCallback(() => selectedGuild &&
        dispatch(playUrl(selectedGuild, url, volume))
            .catch((err) => enqueueSnackbar('Failed to play from URL: ' + err.message, { variant: 'error' })), [selectedGuild, url, volume])
    const onUpdateEntrySound = useCallback((sound: string) => selectedGuild &&
        dispatch(updateEntrySound(selectedGuild, sound))
            .catch((err) => enqueueSnackbar('Failed to update entry sound: ' + err.message, { variant: 'error' })), [selectedGuild])
    const onUpdateExitSound = useCallback((sound: string) => selectedGuild &&
        dispatch(updateExitSound(selectedGuild, sound))
            .catch((err) => enqueueSnackbar('Failed to update exit sound: ' + err.message, { variant: 'error' })), [selectedGuild])
    const onClearEntrySound = useCallback(() => selectedGuild &&
        dispatch(updateEntrySound(selectedGuild))
            .catch((err) => enqueueSnackbar('Failed to clear entry sound: ' + err.message, { variant: 'error' })), [selectedGuild])
    const onClearExitSound = useCallback(() => selectedGuild &&
        dispatch(updateExitSound(selectedGuild))
            .catch((err) => enqueueSnackbar('Failed to clear exit sound: ' + err.message, { variant: 'error' })), [selectedGuild])

    useEffect(() => {
        selectedGuild && dispatch(fetchSounds(selectedGuild))
    }, [selectedGuild])

    const tags = useMemo(() => getTags(sounds), [sounds])

    const onTagFilterChange = (event: React.ChangeEvent<{ value: unknown }>) => {
        setTagFilter(event.target.value as string[])
    }

    const onUrlFieldChange = (event: React.ChangeEvent<HTMLTextAreaElement | HTMLInputElement>) => {
        setUrl(event.target.value)
    }

    const onGroupByChange = (event: React.ChangeEvent<HTMLTextAreaElement | HTMLInputElement>) => {
        setGroupBy(event.target.value as GroupBy)
    }

    return (
        <>
            <Grid container spacing={4}>
                <Grid item xs={12} sm={6}>
                    <FormControl fullWidth component='fieldset'>
                        <FormLabel component='legend'>Group sounds</FormLabel>
                        <RadioGroup row value={groupBy} onChange={onGroupByChange}>
                            <FormControlLabel
                                value='alphabetic'
                                control={<Radio color='primary' />}
                                label='Alphabetically'
                                labelPlacement='start'
                            />
                            <FormControlLabel
                                value='tag'
                                control={<Radio color='primary' />}
                                label='By Tag'
                                labelPlacement='start'
                            />
                        </RadioGroup>
                    </FormControl>
                </Grid>
                <Grid item xs={12} sm={6}>
                    <FormControl fullWidth>
                        <FormLabel component='legend'>Filter by tags</FormLabel>
                        <Select
                            multiple
                            value={tagFilter}
                            onChange={onTagFilterChange}
                            input={<Input />}
                        >
                            {tags.map((tag) => (
                                <MenuItem key={tag} value={tag} selected={tagFilter.includes(tag)}>
                                    {tag}
                                </MenuItem>
                            ))}
                        </Select>
                    </FormControl>
                </Grid>
                <Grid item xs={12} sm={6}>
                    <FormControl fullWidth>
                        <FormLabel component='legend'>Volume</FormLabel>
                        <Slider
                            defaultValue={defVolume}
                            min={1}
                            max={100}
                            valueLabelDisplay='auto'
                            onChangeCommitted={(_event, vol) => setVolume(vol as number)}
                        />
                    </FormControl>
                </Grid>
                <Grid container item xs={12} sm={6} spacing={2} alignItems='flex-end'>
                    <Grid item xs={10}>
                        <FormControl fullWidth>
                            <FormLabel component='legend'>Play from URL</FormLabel>
                            <TextField
                                value={url}
                                onChange={onUrlFieldChange}
                                fullWidth
                            />
                        </FormControl>
                    </Grid>
                    <Grid item xs={2}>
                        <Button
                            fullWidth
                            size='medium'
                            variant='contained'
                            color='primary'
                            onClick={onPlayUrl}
                        >
                            Play
                        </Button>
                    </Grid>
                </Grid>
            </Grid>
            <Divider className={classes.divider} />
            <Grid container justify='center'>
                <Button
                    variant='contained'
                    color='primary'
                    onClick={onPlayRandomSound}
                >
                    Random
                    </Button>

            </Grid>
            {soundsLoading ?
                <Box display='flex' flexDirection='column' alignItems='center' m={8}>
                    <CircularProgress className={classes.loadingIndicator} />
                    <Typography>
                        Loading board...
                    </Typography>
                </Box>
                :
                <Board
                    sounds={sounds}
                    groupBy={groupBy}
                    tagFilter={tagFilter}
                    entrySound={membership?.entrySound || null}
                    exitSound={membership?.exitSound || null}
                    onPlaySound={onPlaySound}
                    onUpdateEntrySound={onUpdateEntrySound}
                    onUpdateExitSound={onUpdateExitSound}
                    onClearEntrySound={onClearEntrySound}
                    onClearExitSound={onClearExitSound}
                />
            }
        </>
    )
}

interface BoardProps {
    sounds: IAudioFile[]
    groupBy: GroupBy
    tagFilter: string[]
    entrySound: string | null
    exitSound: string | null
    onPlaySound: (sound: string) => void
    onUpdateEntrySound: (sound: string) => void
    onUpdateExitSound: (sound: string) => void
    onClearEntrySound: () => void
    onClearExitSound: () => void
}

const Board: React.FC<BoardProps> = React.memo((props) => {

    const {
        sounds,
        groupBy,
        tagFilter,
        entrySound,
        exitSound,
        onPlaySound,
        onUpdateEntrySound,
        onUpdateExitSound,
        onClearEntrySound,
        onClearExitSound
    } = props

    const classes = useStyles()

    const groupedSounds = useMemo(() => filterAndGroup(tagFilter, groupBy, sounds), [sounds, groupBy, tagFilter])
    const keys = useMemo(() => R.keys(groupedSounds).sort(), [groupedSounds])

    const [anchorEl, setAnchorEl] = React.useState<null | HTMLElement>(null)
    const [sound, setSound] = React.useState<null | string>(null)

    const handleContextClick = (sound: string) => (event: React.MouseEvent<HTMLButtonElement>) => {
        event.preventDefault()
        setAnchorEl(event.currentTarget)
        setSound(sound)
    }

    const handleClose = () => setAnchorEl(null)

    const handleUpdateEntrySound = (sound: string) => {
        handleClose()
        onUpdateEntrySound(sound)
    }

    const handleUpdateExitSound = (sound: string) => {
        handleClose()
        onUpdateExitSound(sound)
    }

    const handleClearEntrySound = () => {
        handleClose()
        onClearEntrySound()
    }

    const handleClearExitSound = () => {
        handleClose()
        onClearExitSound()
    }

    return (
        <>
            {keys.map((key) =>
                <React.Fragment key={key}>
                    <Divider>{key}</Divider>
                    <Box display='flex' flexWrap='wrap'>
                        {groupedSounds[key].map(({ name }) =>
                            <Box key={name} boxShadow={2} mr={1} mt={1}>
                                <CardActionArea
                                    onContextMenu={handleContextClick(name)}
                                    className={classes.card}
                                    onClick={() => onPlaySound(name)}
                                >
                                    {name}
                                </CardActionArea>
                            </Box>
                        )}
                    </Box>
                </React.Fragment>
            )}
            <Menu
                keepMounted
                open={Boolean(anchorEl)}
                onClose={handleClose}
                anchorEl={anchorEl}
            >
                <MenuItem onClick={() => sound && handleUpdateEntrySound(sound)}>Set as entry sound</MenuItem>
                <MenuItem onClick={() => sound && handleUpdateExitSound(sound)}>Set as exit sound</MenuItem>
                <MenuItem disabled={!entrySound} onClick={handleClearEntrySound}>Clear entry sound {entrySound ? ` (${entrySound})` : ''}</MenuItem>
                <MenuItem disabled={!exitSound} onClick={handleClearExitSound}>Clear exit sound {exitSound ? ` (${exitSound})` : ''}</MenuItem>
            </Menu>
        </>
    )
})

export default Soundboard