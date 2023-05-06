package com.github.sipe90.sackbot.audio.event

import com.sedmelluq.discord.lavaplayer.track.AudioTrack

data class TrackEndEvent(override val guildId: String, val track: String) : GuildVoiceEvent() {

    companion object {
        fun fromAudioTrack(guildId: String, audioTrack: AudioTrack): TrackEndEvent = TrackEndEvent(guildId, audioTrack.identifier)
    }
}
