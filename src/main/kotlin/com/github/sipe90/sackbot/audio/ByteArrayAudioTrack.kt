package com.github.sipe90.sackbot.audio

import com.sedmelluq.discord.lavaplayer.container.MediaContainerDescriptor
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManager
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo
import com.sedmelluq.discord.lavaplayer.track.DelegatedAudioTrack
import com.sedmelluq.discord.lavaplayer.track.InternalAudioTrack
import com.sedmelluq.discord.lavaplayer.track.playback.LocalAudioTrackExecutor

class ByteArrayAudioTrack(
        private val audio: ByteArray,
        trackInfo: AudioTrackInfo,
        val containerTrackFactory: MediaContainerDescriptor,
        private val sourceManager: NitriteAudioSourceManager
) : DelegatedAudioTrack(trackInfo) {

    override fun process(localExecutor: LocalAudioTrackExecutor?) {
        ByteArraySeekableInputStream(audio).use { inputStream ->
            processDelegate(
                    containerTrackFactory.createTrack(
                            trackInfo,
                            inputStream
                    ) as InternalAudioTrack, localExecutor
            )
        }
    }

    override fun makeShallowClone(): AudioTrack {
        return ByteArrayAudioTrack(audio, trackInfo, containerTrackFactory, sourceManager)
    }

    override fun getSourceManager(): AudioSourceManager? {
        return sourceManager
    }
}