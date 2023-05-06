package com.github.sipe90.sackbot.audio.event

import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Sinks

@Component
class GuildVoiceEventEmitter {

    private val guildVoiceEventSink = Sinks.unsafe().many().multicast().directBestEffort<GuildVoiceEvent>()

    fun onTrackStart(guildId: String, track: AudioTrack) {
        publish(TrackStartEvent.fromAudioTrack(guildId, track))
    }

    fun onTrackEnd(guildId: String, track: AudioTrack) {
        publish(TrackEndEvent.fromAudioTrack(guildId, track))
    }

    fun onVolumeChange(guildId: String, volume: Int) {
        publish(VolumeChangeEvent(guildId, volume))
    }

    fun onVoiceChannelChange(guildId: String, channelLeft: String?, channelJoined: String?) {
        publish(VoiceChannelEvent(guildId, channelLeft, channelJoined))
    }

    fun subscribe(guildId: String): Flux<GuildVoiceEvent> =
        guildVoiceEventSink.asFlux().filter { event -> event.guildId == guildId }

    private fun publish(event: GuildVoiceEvent) {
        guildVoiceEventSink.tryEmitNext(event)
    }
}
