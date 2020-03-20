import React, { useEffect } from 'react'
import { useDispatch, useSelector } from '@/util'
import { fetchUser, fetchGuilds, selectGuild } from '@/actions/user'
import App from '@/components/App'
import { playSound, playRandomSound } from '@/actions/sounds'

const AppContainer: React.FC = () => {
 
    const state = useSelector((state) => ({
        memberships: state.user.memberships,
        guilds: state.user.guilds,
        selectedGuild: state.user.selectedGuild,
        sounds: state.sounds.sounds,
        playingSound: state.sounds.playingSound
    }))

    const dispatch = useDispatch()

    useEffect(() => {
        dispatch(fetchUser())
        dispatch(fetchGuilds())
    }, [])

    return <App 
        guilds={state.guilds}
        selectedGuild={state.selectedGuild}
        onGuildSelect={(guildId) => dispatch(selectGuild(guildId))}
        sounds={state.sounds}
        onPlaySound={(name) => dispatch(playSound(state.selectedGuild, name))}
        onPlayRandomSound={() => dispatch(playRandomSound(state.selectedGuild))}
        playingSound={state.playingSound}
    />
}

export default AppContainer