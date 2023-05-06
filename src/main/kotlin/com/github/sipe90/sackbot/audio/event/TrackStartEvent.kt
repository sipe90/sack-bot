package com.github.sipe90.sackbot.audio.event

import com.sedmelluq.discord.lavaplayer.track.AudioTrack

data class TrackStartEvent(override val guildId: String, val track: String) : GuildVoiceEvent() {

    companion object {
        fun fromAudioTrack(guildId: String, audioTrack: AudioTrack): TrackStartEvent = TrackStartEvent(guildId, audioTrack.identifier)
    }
}
