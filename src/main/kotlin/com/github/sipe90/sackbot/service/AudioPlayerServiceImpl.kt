package com.github.sipe90.sackbot.service

import com.github.sipe90.sackbot.component.LavaPlayerManager
import com.github.sipe90.sackbot.exception.ValidationException
import com.github.sipe90.sackbot.util.getVoiceChannel
import com.sedmelluq.discord.lavaplayer.track.AudioItem
import mu.KotlinLogging
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class AudioPlayerServiceImpl(
    private val jdaService: JDAService,
    private val playerManager: LavaPlayerManager,
) : AudioPlayerService {
    private val logger = KotlinLogging.logger {}

    override fun playAudioForUser(
        guildId: String,
        userId: String,
        name: String,
    ): Mono<Unit> {
        val user = jdaService.getUser(userId) ?: throw IllegalArgumentException("Invalid user id")
        val guild = jdaService.getGuild(guildId) ?: throw IllegalArgumentException("Invalid guild id")
        val voiceChannel =
            getVoiceChannel(guild, user) ?: throw ValidationException("Could not find voice channel to play in")

        return playAudioInChannel(name, voiceChannel)
    }

    override fun playAudioInChannel(
        name: String,
        audioChannel: AudioChannel,
    ): Mono<Unit> {
        return playerManager.playDatabaseTrack(name, audioChannel).then(Mono.empty())
    }

    override fun playUrlForUser(
        guildId: String,
        userId: String,
        url: String,
    ): Mono<AudioItem> {
        val user = jdaService.getUser(userId) ?: throw IllegalArgumentException("Invalid user id")
        val voiceChannel =
            getVoiceChannel(user) ?: throw ValidationException("Could not find voice channel to play in")

        return playUrlInChannel(url, voiceChannel)
    }

    override fun playUrlInChannel(
        url: String,
        audioChannel: AudioChannel,
    ): Mono<AudioItem> {
        return playerManager.playExternalTrack(url, audioChannel)
    }

    override fun setVolume(
        guildId: String,
        volume: Int,
    ): Mono<Unit> {
        val guild = jdaService.getGuild(guildId) ?: throw IllegalArgumentException("Invalid guild id")

        return Mono.defer {
            logger.debug { "Updating player volume to $volume for guild $guildId" }
            playerManager.setVolume(guild, volume)
        }
    }

    override fun getVolume(guildId: String): Int {
        val guild = jdaService.getGuild(guildId) ?: throw IllegalArgumentException("Invalid guild id")

        return playerManager.getVolume(guild)
    }
}
