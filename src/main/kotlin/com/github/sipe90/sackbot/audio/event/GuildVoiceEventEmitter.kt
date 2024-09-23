package com.github.sipe90.sackbot.audio.event

import com.github.sipe90.sackbot.util.Initiator
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Sinks

@Component
class GuildVoiceEventEmitter {
    private val guildVoiceEventSink = Sinks.unsafe().many().multicast().directBestEffort<GuildVoiceEvent>()

    fun onTrackStart(
        guildId: String,
        track: AudioTrack,
    ) {
        val initiator = track.userData as Initiator?
        publish(TrackStartEvent(guildId, initiator?.name, initiator?.avatarUrl, track.info.title))
    }

    fun onTrackEnd(
        guildId: String,
        track: AudioTrack,
    ) {
        val initiator = track.userData as Initiator?
        publish(TrackEndEvent(guildId, initiator?.name, initiator?.avatarUrl, track.info.title))
    }

    fun onVolumeChange(
        guildId: String,
        initiator: Initiator?,
        volume: Int,
    ) {
        publish(VolumeChangeEvent(guildId, initiator?.name, initiator?.avatarUrl, volume))
    }

    fun onVoiceChannelChange(
        guildId: String,
        initiator: Initiator?,
        channelLeft: String?,
        channelJoined: String?,
    ) {
        publish(VoiceChannelEvent(guildId, initiator?.name, initiator?.avatarUrl, channelLeft, channelJoined))
    }

    fun subscribe(guildId: String): Flux<GuildVoiceEvent> = guildVoiceEventSink.asFlux().filter { event -> event.guildId == guildId }

    private fun publish(event: GuildVoiceEvent) {
        guildVoiceEventSink.tryEmitNext(event)
    }
}
