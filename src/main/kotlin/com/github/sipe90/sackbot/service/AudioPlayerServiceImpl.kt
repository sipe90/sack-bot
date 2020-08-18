package com.github.sipe90.sackbot.service

import com.github.sipe90.sackbot.audio.ByteArrayAudioTrack
import com.github.sipe90.sackbot.audio.ByteArraySeekableInputStream
import com.github.sipe90.sackbot.audio.ContainerRegistries
import com.github.sipe90.sackbot.component.LavaPlayerManager
import com.github.sipe90.sackbot.component.TTS
import com.github.sipe90.sackbot.component.VoiceLines
import com.github.sipe90.sackbot.exception.ValidationException
import com.github.sipe90.sackbot.util.getVoiceChannel
import com.sedmelluq.discord.lavaplayer.container.MediaContainerDetection
import com.sedmelluq.discord.lavaplayer.container.MediaContainerHints
import com.sedmelluq.discord.lavaplayer.track.AudioItem
import com.sedmelluq.discord.lavaplayer.track.AudioReference
import net.dv8tion.jda.api.entities.VoiceChannel
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono


@Service
class AudioPlayerServiceImpl(
        private val jdaService: JDAService,
        private val tts: TTS,
        private val voiceLines: VoiceLines,
        private val playerManager: LavaPlayerManager
) : AudioPlayerService {

    private final val logger = LoggerFactory.getLogger(javaClass)

    override fun playAudioForUser(guildId: String, userId: String, name: String, volume: Int?): Mono<Void> {
        val user = jdaService.getUser(userId) ?: throw IllegalArgumentException("Invalid user id")
        val guild = jdaService.getGuild(guildId) ?: throw IllegalArgumentException("Invalid guild id")
        val voiceChannel =
                getVoiceChannel(guild, user) ?: throw ValidationException("Could not find voice channel to play in")
        return playAudioInChannel(name, voiceChannel, volume)
    }

    override fun playAudioInChannel(name: String, voiceChannel: VoiceChannel, volume: Int?): Mono<Void> {
        val identifier = "${voiceChannel.guild.id}:${name}"
        return playerManager.playNitriteTrack(identifier, voiceChannel, volume).then()
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

    override fun playVoiceLinesInChannel(voice: String, lines: List<String>, voiceChannel: VoiceChannel): Mono<Void> {
        val paths = voiceLines.getPaths(voice, lines)

        logger.debug("Queuing voice lines {} from voice {} on channel #{}", lines, voice, voiceChannel.name)

        val monos = paths.map { playerManager.queueLocalTrack(it.toString(), voiceChannel, null) }

        return Flux.concat(monos).then()
    }

    override fun getAvailableVoices(): Set<String> = tts.getAvailableVoices()

    override fun isRandomTtsEnabled(): Boolean = tts.isRandomEnabled()

    override fun playRandomTtsForUser(guildId: String, userId: String, voice: String): Mono<Void> {
        val user = jdaService.getUser(userId) ?: throw IllegalArgumentException("Invalid user id")
        val voiceChannel = getVoiceChannel(user) ?: throw ValidationException("Could not find voice channel to play in")
        return playRandomTtsInChannel(voice, voiceChannel)
    }

    override fun playRandomTtsInChannel(voice: String, voiceChannel: VoiceChannel): Mono<Void> {
        return tts.randomPhraseToSpeech(voice)
                .map(this::detectWavTrack)
                .flatMap { playerManager.playTrack(it, voiceChannel, null) }.then()
    }

    override fun playTtsForUser(guildId: String, userId: String, voice: String, text: String): Mono<Void> {
        val user = jdaService.getUser(userId) ?: throw IllegalArgumentException("Invalid user id")
        val voiceChannel = getVoiceChannel(user) ?: throw ValidationException("Could not find voice channel to play in")
        return playTtsInChannel(voice, text, voiceChannel)
    }

    override fun playTtsInChannel(voice: String, text: String, voiceChannel: VoiceChannel): Mono<Void> {
        return tts.textToSpeech(voice, text)
                .map(this::detectWavTrack)
                .flatMap { playerManager.playTrack(it, voiceChannel, null) }.then()
    }

    override fun playUrlForUser(guildId: String, userId: String, url: String, volume: Int?): Mono<AudioItem> {
        val user = jdaService.getUser(userId) ?: throw IllegalArgumentException("Invalid user id")
        val voiceChannel = getVoiceChannel(user) ?: throw ValidationException("Could not find voice channel to play in")
        return playUrlInChannel(url, voiceChannel, volume)
    }

    override fun playUrlInChannel(url: String, voiceChannel: VoiceChannel, volume: Int?): Mono<AudioItem> {
        return playerManager.playExternalTrack(url, voiceChannel, volume)
    }

    override fun setDefaultVolume(guildId: String, volume: Int) = playerManager.setDefaultVolume(guildId, volume)

    override fun getDefaultVolume(guildId: String) = playerManager.getDefaultVolume(guildId)

    private fun detectWavTrack(data: ByteArray): ByteArrayAudioTrack {
        return ByteArraySeekableInputStream(data).use {
            val result = MediaContainerDetection(
                    ContainerRegistries.wav,
                    AudioReference("tts", null),
                    it,
                    MediaContainerHints.from(null, "wav")
            ).detectContainer()
            ByteArrayAudioTrack(data, result.trackInfo, result.containerDescriptor, null)
        }
    }
}

