package com.github.sipe90.sackbot.service

import com.github.sipe90.sackbot.audio.NitriteAudioSourceManager
import com.github.sipe90.sackbot.audio.TrackScheduler
import com.github.sipe90.sackbot.component.TTS
import com.github.sipe90.sackbot.component.VoiceLines
import com.github.sipe90.sackbot.exception.ValidationException
import com.github.sipe90.sackbot.util.getVoiceChannel
import com.sedmelluq.discord.lavaplayer.natives.ConnectorNativeLibLoader
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager
import com.sedmelluq.discord.lavaplayer.player.FunctionalResultHandler
import com.sedmelluq.discord.lavaplayer.source.beam.BeamAudioSourceManager
import com.sedmelluq.discord.lavaplayer.source.local.LocalAudioSourceManager
import com.sedmelluq.discord.lavaplayer.source.twitch.TwitchStreamAudioSourceManager
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager
import com.sedmelluq.discord.lavaplayer.track.AudioReference
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import com.sedmelluq.discord.lavaplayer.track.playback.MutableAudioFrame
import net.dv8tion.jda.api.audio.AudioSendHandler
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.VoiceChannel
import net.dv8tion.jda.api.managers.AudioManager
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.nio.ByteBuffer


@Service
class AudioPlayerServiceImpl(
    private val jdaService: JDAService,
    private val tts: TTS,
    private val voiceLines: VoiceLines,
    nitriteManager: NitriteAudioSourceManager
) : AudioPlayerService {

    private final val logger = LoggerFactory.getLogger(javaClass)

    private final val localAudioSourceManager = LocalAudioSourceManager()
    private final val playerManager: DefaultAudioPlayerManager = DefaultAudioPlayerManager()
    private final val trackSchedulers = mutableMapOf<String, TrackScheduler>()

    init {
        playerManager.registerSourceManager(nitriteManager)
        playerManager.registerSourceManager(localAudioSourceManager)
        playerManager.registerSourceManager(YoutubeAudioSourceManager())
        playerManager.registerSourceManager(TwitchStreamAudioSourceManager())
        playerManager.registerSourceManager(BeamAudioSourceManager())

        ConnectorNativeLibLoader.loadConnectorLibrary()
    }

    override fun playAudioForUser(guildId: String, userId: String, name: String, volume: Int?): Mono<Void> {
        val user = jdaService.getUser(userId) ?: throw IllegalArgumentException("Invalid user id")
        val guild = jdaService.getGuild(guildId) ?: throw IllegalArgumentException("Invalid guild id")
        val voiceChannel =
            getVoiceChannel(guild, user) ?: throw ValidationException("Could not find voice channel to play in")
        return playAudioInChannel(name, voiceChannel, volume)
    }

    override fun playAudioForUser(userId: String, name: String, volume: Int?): Mono<Void> {
        val user = jdaService.getUser(userId) ?: throw IllegalArgumentException("Invalid user id")
        val voiceChannel = getVoiceChannel(user) ?: throw ValidationException("Could not find voice channel to play in")
        return playAudioInChannel(name, voiceChannel, volume)
    }

    override fun playAudioInChannel(name: String, voiceChannel: VoiceChannel, volume: Int?): Mono<Void> {
        return playInChannel("${voiceChannel.guild.id}:${name}", voiceChannel, volume).then()
    }

    override fun playVoiceLinesForUser(
        guildId: String,
        userId: String,
        voice: String,
        lines: List<String>
    ): Mono<Void> {
        val user = jdaService.getUser(userId) ?: throw IllegalArgumentException("Invalid user id")
        val voiceChannel = getVoiceChannel(user) ?: throw ValidationException("Could not find voice channel to play in")
        return playVoiceLinesInChannel(voice, lines, voiceChannel)
    }

    override fun playVoiceLinesInChannel(voice: String, lines: List<String>, voiceChannel: VoiceChannel): Mono<Void> =
        Mono.fromCallable {
            val guild = voiceChannel.guild
            val audioManager = guild.audioManager
            val trackScheduler = trackSchedulers.getOrPut(guild.id, { createScheduler(guild) })

            val paths = voiceLines.getPaths(voice, lines)

            connect(audioManager, voiceChannel, trackScheduler.player)

            logger.debug("Queuing voice lines {} from voice {} on channel #{}", lines, voice, voiceChannel.name)

            paths.forEach {
                val track =
                    localAudioSourceManager.loadItem(playerManager, AudioReference(it.toString(), null)) as AudioTrack
                trackScheduler.queue(track, null)
            }
        }.then()

    override fun playRandomTtsInChannel(voiceChannel: VoiceChannel): Mono<Boolean> {
        return tts.randomPhraseToSpeech().flatMap { playInChannel(it.toString(), voiceChannel, null) }.defaultIfEmpty(false)
    }

    override fun playRandomTtsForUser(guildId: String, userId: String): Mono<Boolean> {
        val user = jdaService.getUser(userId) ?: throw IllegalArgumentException("Invalid user id")
        val voiceChannel = getVoiceChannel(user) ?: throw ValidationException("Could not find voice channel to play in")
        return playRandomTtsInChannel(voiceChannel)
    }

    override fun playTtsInChannel(text: String, voiceChannel: VoiceChannel): Mono<Boolean> {
        return tts.textToSpeech(text).flatMap { playInChannel(it.toString(), voiceChannel, null) }
    }

    override fun playTtsForUser(guildId: String, userId: String, text: String): Mono<Boolean> {
        val user = jdaService.getUser(userId) ?: throw IllegalArgumentException("Invalid user id")
        val voiceChannel = getVoiceChannel(user) ?: throw ValidationException("Could not find voice channel to play in")
        return playTtsInChannel(text, voiceChannel)
    }

    override fun playUrlInChannel(url: String, voiceChannel: VoiceChannel, volume: Int?): Mono<Boolean> {
        return playInChannel(url, voiceChannel, volume)
    }

    private fun playInChannel(identifier: String, voiceChannel: VoiceChannel, volume: Int?): Mono<Boolean> {
        val guild = voiceChannel.guild
        val audioManager = guild.audioManager
        val trackScheduler = trackSchedulers.getOrPut(guild.id, { createScheduler(guild) })

        connect(audioManager, voiceChannel, trackScheduler.player)

        logger.debug("Looking for track with identifier: {}", identifier)

        return Mono.create { sink ->
            playerManager.loadItem(identifier, FunctionalResultHandler(
                {
                    logger.debug("Playing track {} on channel #{}", it.info.title, voiceChannel.name)
                    trackScheduler.interrupt(it, volume)
                    sink.success(true)
                },
                {
                    logger.debug("Found playlist with tracks {}", it.tracks.map { track -> track.info.title })
                    sink.success(false)
                },
                {
                    logger.warn("Could not find track with identifier {}", identifier)
                    sink.success(false)
                },
                { e ->
                    logger.error("Exception while trying to load track", e)
                    sink.error(e)
                }
            ))
        }
    }

    override fun setVolume(guildId: String, volume: Int) {
        val scheduler = trackSchedulers[guildId]
        if (scheduler == null) {
            logger.debug("Could not set volume for guild {}: scheduler not found", guildId)
            return
        }
        logger.debug("Setting default volume for guild {} player to {}", guildId, volume)
        scheduler.defaultVolume = volume
    }

    override fun getVolume(guildId: String): Int? {
        val scheduler = trackSchedulers[guildId]
        if (scheduler == null) {
            logger.debug("Could not get default volume for guild {}: scheduler not found", guildId)
            return null
        }
        return scheduler.defaultVolume
    }

    private fun createScheduler(guild: Guild): TrackScheduler {
        logger.debug("Creating new instance of TrackScheduler for Guild {} ({})", guild.name, guild.id)
        val audioPlayer = playerManager.createPlayer()
        val scheduler = TrackScheduler(audioPlayer)
        audioPlayer.addListener(scheduler)
        return scheduler
    }

    private fun connect(audioManager: AudioManager, voiceChannel: VoiceChannel, audioPlayer: AudioPlayer) {
        if (audioManager.isAttemptingToConnect) {
            val queued = audioManager.queuedAudioConnection
            if (queued != null && queued.id != voiceChannel.id) {
                audioManager.closeAudioConnection()
            }
        }

        audioManager.openAudioConnection(voiceChannel)
        audioManager.sendingHandler = AudioPlayerSendHandler(audioPlayer)
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

