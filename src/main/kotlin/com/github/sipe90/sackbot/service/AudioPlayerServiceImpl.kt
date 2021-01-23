package com.github.sipe90.sackbot.service

import com.github.sipe90.sackbot.component.LavaPlayerManager
import com.github.sipe90.sackbot.exception.ValidationException
import com.github.sipe90.sackbot.util.getVoiceChannel
import com.sedmelluq.discord.lavaplayer.track.AudioItem
import net.dv8tion.jda.api.entities.VoiceChannel
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono


@Service
class AudioPlayerServiceImpl(
    private val jdaService: JDAService,
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
}

