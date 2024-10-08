package com.github.sipe90.sackbot.audio

import com.github.sipe90.sackbot.persistence.dto.AudioFile
import com.github.sipe90.sackbot.service.AudioFileService
import com.sedmelluq.discord.lavaplayer.container.MediaContainerDescriptor
import com.sedmelluq.discord.lavaplayer.container.MediaContainerDetection
import com.sedmelluq.discord.lavaplayer.container.MediaContainerHints
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager
import com.sedmelluq.discord.lavaplayer.source.ProbingAudioSourceManager
import com.sedmelluq.discord.lavaplayer.track.AudioItem
import com.sedmelluq.discord.lavaplayer.track.AudioReference
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo
import org.springframework.stereotype.Component
import java.io.DataInput
import java.io.DataOutput

@Component
class DatabaseAudioSourceManager(private val audioFileService: AudioFileService) :
    ProbingAudioSourceManager(ContainerRegistries.audio) {
    override fun getSourceName() = "db"

    override fun loadItem(
        manager: AudioPlayerManager,
        reference: AudioReference,
    ): AudioItem? {
        val audioFile = getAudioFile(reference.identifier) ?: return null
        ByteArraySeekableInputStream(audioFile.data).use {
            return handleLoadResult(
                MediaContainerDetection(
                    containerRegistry,
                    reference,
                    it,
                    MediaContainerHints.from(null, audioFile.extension),
                ).detectContainer(),
            )
        }
    }

    override fun isTrackEncodable(track: AudioTrack): Boolean {
        return true
    }

    override fun encodeTrack(
        track: AudioTrack,
        output: DataOutput,
    ) {
        encodeTrackFactory((track as ByteArrayAudioTrack).containerTrackFactory, output)
    }

    override fun createTrack(
        trackInfo: AudioTrackInfo,
        containerTrackFactory: MediaContainerDescriptor,
    ): AudioTrack? {
        val audioFile = getAudioFile(trackInfo.identifier) ?: return null
        return ByteArrayAudioTrack(audioFile.data, trackInfo, containerTrackFactory, this)
    }

    override fun decodeTrack(
        trackInfo: AudioTrackInfo,
        input: DataInput,
    ): AudioTrack? {
        val containerTrackFactory = decodeTrackFactory(input)
        return if (containerTrackFactory != null) {
            val audioFile = getAudioFile(trackInfo.identifier) ?: return null
            ByteArrayAudioTrack(audioFile.data, trackInfo, containerTrackFactory, this)
        } else {
            null
        }
    }

    override fun shutdown() {
        // Nothing to do here
    }

    private fun getAudioFile(identifier: String): AudioFile? {
        val id = identifier.split(":")
        if (id.size != 2) return null
        return audioFileService.findAudioFile(id[0], id[1]).block()
    }
}
