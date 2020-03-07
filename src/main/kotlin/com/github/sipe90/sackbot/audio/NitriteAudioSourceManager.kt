package com.github.sipe90.sackbot.audio

import com.github.sipe90.sackbot.persistence.dto.AudioFile
import com.github.sipe90.sackbot.service.AudioFileService
import com.sedmelluq.discord.lavaplayer.container.MediaContainerDescriptor
import com.sedmelluq.discord.lavaplayer.container.MediaContainerDetection
import com.sedmelluq.discord.lavaplayer.container.MediaContainerHints
import com.sedmelluq.discord.lavaplayer.container.MediaContainerRegistry
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager
import com.sedmelluq.discord.lavaplayer.source.ProbingAudioSourceManager
import com.sedmelluq.discord.lavaplayer.track.AudioItem
import com.sedmelluq.discord.lavaplayer.track.AudioReference
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo
import org.springframework.stereotype.Component
import java.io.DataInput
import java.io.DataOutput

@Component
class NitriteAudioSourceManager(private val audioFileService: AudioFileService) :
    ProbingAudioSourceManager(MediaContainerRegistry.DEFAULT_REGISTRY) {

    override fun getSourceName() = "nitrite"

    override fun loadItem(manager: DefaultAudioPlayerManager, reference: AudioReference): AudioItem? {
        val audioFile = getAudioFile(reference.identifier) ?: return null
        SeekableBufferedInputStream(audioFile.data.clone()).use {
            return handleLoadResult(
                MediaContainerDetection(
                    containerRegistry,
                    reference,
                    it,
                    MediaContainerHints.from(null, audioFile.extension)
                ).detectContainer()
            )
        }
    }

    override fun isTrackEncodable(track: AudioTrack): Boolean {
        return true
    }

    override fun encodeTrack(track: AudioTrack, output: DataOutput) {
        encodeTrackFactory((track as ByteArrayAudioTrack).containerTrackFactory, output)
    }

    override fun createTrack(trackInfo: AudioTrackInfo, containerTrackFactory: MediaContainerDescriptor): AudioTrack? {
        val audioFile = getAudioFile(trackInfo.identifier) ?: return null
        return ByteArrayAudioTrack(audioFile.data, trackInfo, containerTrackFactory, this)
    }

    override fun decodeTrack(trackInfo: AudioTrackInfo, input: DataInput): AudioTrack? {
        val containerTrackFactory = decodeTrackFactory(input)
        return if (containerTrackFactory != null) {
            val audioFile = getAudioFile(trackInfo.identifier) ?: return null
            ByteArrayAudioTrack(audioFile.data, trackInfo, containerTrackFactory, this)
        } else null
    }

    private fun getAudioFile(identifier: String): AudioFile? {
        val id = identifier.split(":")
        if (id.size != 2) return null
        return audioFileService.findAudioFile(id[0], id[1]).block()
    }

    override fun shutdown() {}
}