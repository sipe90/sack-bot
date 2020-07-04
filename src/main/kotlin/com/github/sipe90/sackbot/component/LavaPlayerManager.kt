package com.github.sipe90.sackbot.component

import com.github.sipe90.sackbot.audio.NitriteAudioSourceManager
import com.github.sipe90.sackbot.audio.TrackScheduler
import com.github.sipe90.sackbot.exception.NotFoundException
import com.sedmelluq.discord.lavaplayer.natives.ConnectorNativeLibLoader
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager
import com.sedmelluq.discord.lavaplayer.player.FunctionalResultHandler
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManager
import com.sedmelluq.discord.lavaplayer.source.bandcamp.BandcampAudioSourceManager
import com.sedmelluq.discord.lavaplayer.source.beam.BeamAudioSourceManager
import com.sedmelluq.discord.lavaplayer.source.getyarn.GetyarnAudioSourceManager
import com.sedmelluq.discord.lavaplayer.source.http.HttpAudioSourceManager
import com.sedmelluq.discord.lavaplayer.source.local.LocalAudioSourceManager
import com.sedmelluq.discord.lavaplayer.source.stream.M3uStreamSegmentUrlProvider
import com.sedmelluq.discord.lavaplayer.source.twitch.TwitchStreamAudioSourceManager
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager
import com.sedmelluq.discord.lavaplayer.track.AudioItem
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioReference
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import com.sedmelluq.discord.lavaplayer.track.playback.MutableAudioFrame
import kotlinx.coroutines.reactor.mono
import net.dv8tion.jda.api.audio.AudioSendHandler
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.VoiceChannel
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import java.nio.ByteBuffer

@Component
class LavaPlayerManager(private val nitriteManager: NitriteAudioSourceManager) {

    private final val logger = LoggerFactory.getLogger(javaClass)

    private final val localAudioSourceManager = LocalAudioSourceManager()
    private final val playerManager = DefaultAudioPlayerManager()
    private final val trackSchedulers = mutableMapOf<String, TrackScheduler>()

    init {
        playerManager.registerSourceManager(YoutubeAudioSourceManager())
        playerManager.registerSourceManager(TwitchStreamAudioSourceManager())
        playerManager.registerSourceManager(BeamAudioSourceManager())
        playerManager.registerSourceManager(BandcampAudioSourceManager())
        playerManager.registerSourceManager(GetyarnAudioSourceManager())
        playerManager.registerSourceManager(HttpAudioSourceManager())

        ConnectorNativeLibLoader.loadConnectorLibrary()
    }

    fun playNitriteTrack(identifier: String, voiceChannel: VoiceChannel, volume: Int?): Mono<AudioTrack> =
            playTrack(nitriteManager, identifier, voiceChannel, false, volume)

    fun playLocalTrack(identifier: String, voiceChannel: VoiceChannel, volume: Int?): Mono<AudioTrack> =
            playTrack(localAudioSourceManager, identifier, voiceChannel, false, volume)

    fun queueLocalTrack(identifier: String, voiceChannel: VoiceChannel, volume: Int?): Mono<AudioTrack> =
            playTrack(localAudioSourceManager, identifier, voiceChannel, true, volume)

    private fun playTrack(
            sourceManager: AudioSourceManager,
            identifier: String,
            voiceChannel: VoiceChannel,
            queue: Boolean,
            volume: Int?
    ): Mono<AudioTrack> = Mono.defer {
        val guild = voiceChannel.guild
        val trackScheduler = getScheduler(guild)

        connect(voiceChannel, trackScheduler.player)

        mono {
            sourceManager.loadItem(playerManager, AudioReference(identifier, null)) as AudioTrack?
        }.doOnSuccess{ if (queue) trackScheduler.queue(it, volume) else trackScheduler.interrupt(it, volume) }
    }

    fun playExternalTrack(identifier: String, voiceChannel: VoiceChannel, volume: Int?): Mono<AudioItem> {
        val guild = voiceChannel.guild
        val trackScheduler = getScheduler(guild)

        connect(voiceChannel, trackScheduler.player)

        return loadExternalTrack(identifier).doOnSuccess {
            if (it is AudioTrack) trackScheduler.interrupt(it, volume)
            else if (it is AudioPlaylist) trackScheduler.interrupt(it, volume)
        }
    }

    private fun loadExternalTrack(identifier: String): Mono<AudioItem> =
        Mono.create { sink ->
            playerManager.loadItem(identifier, FunctionalResultHandler(
                    {
                        logger.debug("Found external track {}", it.info.title)
                        sink.success(it)
                    },
                    {
                        logger.debug("Found external playlist with tracks {}", it.tracks.map { track -> track.info.title })
                        sink.success(it)
                    },
                    {
                        logger.warn("Could not find external track with identifier {}", identifier)
                        sink.error(NotFoundException("No external track found"))
                    },
                    { e ->
                        logger.error("Exception while trying to external load track", e)
                        sink.error(e)
                    }
            ))
        }

    private fun getScheduler(guild: Guild) = trackSchedulers.getOrPut(guild.id) {
        logger.debug("Creating new instance of TrackScheduler for Guild {} ({})", guild.name, guild.id)
        val audioPlayer = playerManager.createPlayer()
        val scheduler = TrackScheduler(audioPlayer)
        audioPlayer.addListener(scheduler)
        scheduler
    }

    private fun connect(voiceChannel: VoiceChannel, audioPlayer: AudioPlayer) {
        val audioManager = voiceChannel.guild.audioManager
        if (audioManager.isAttemptingToConnect) {
            val queued = audioManager.queuedAudioConnection
            if (queued != null && queued.id != voiceChannel.id) {
                audioManager.closeAudioConnection()
            }
        }

        audioManager.openAudioConnection(voiceChannel)
        audioManager.sendingHandler = AudioPlayerSendHandler(audioPlayer)
    }

    fun setDefaultVolume(guildId: String, volume: Int) {
        val scheduler = trackSchedulers[guildId]
        if (scheduler == null) {
            logger.debug("Could not set volume for guild {}: scheduler not found", guildId)
            return
        }
        logger.debug("Setting default volume for guild {} player to {}", guildId, volume)
        scheduler.defaultVolume = volume
    }

    fun getDefaultVolume(guildId: String): Int? {
        val scheduler = trackSchedulers[guildId]
        if (scheduler == null) {
            logger.debug("Could not get default volume for guild {}: scheduler not found", guildId)
            return null
        }
        return scheduler.defaultVolume
    }

    private class AudioPlayerSendHandler(private val audioPlayer: AudioPlayer) : AudioSendHandler {
        private val buffer: ByteBuffer = ByteBuffer.allocate(1024)
        private val frame: MutableAudioFrame = MutableAudioFrame()

        init {
            frame.setBuffer(buffer)
        }

        override fun isOpus() = true
        override fun provide20MsAudio(): ByteBuffer = buffer.flip()
        override fun canProvide(): Boolean = audioPlayer.provide(frame)
    }

}