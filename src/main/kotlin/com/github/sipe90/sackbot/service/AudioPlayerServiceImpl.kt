package com.github.sipe90.sackbot.service

import com.sedmelluq.discord.lavaplayer.natives.ConnectorNativeLibLoader
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager
import com.sedmelluq.discord.lavaplayer.player.FunctionalResultHandler
import com.sedmelluq.discord.lavaplayer.source.local.LocalAudioSourceManager
import com.sedmelluq.discord.lavaplayer.source.twitch.TwitchStreamAudioSourceManager
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager
import com.sedmelluq.discord.lavaplayer.track.playback.AudioFrame
import net.dv8tion.jda.api.audio.AudioSendHandler
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.VoiceChannel
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.nio.ByteBuffer

@Service
class AudioPlayerServiceImpl : AudioPlayerService {

    private final val logger = LoggerFactory.getLogger(javaClass)

    private final val playerManager: AudioPlayerManager = DefaultAudioPlayerManager()
    private final val audioPlayers = mutableMapOf<String, AudioPlayer>()

    init {
        playerManager.registerSourceManager(LocalAudioSourceManager())
        playerManager.registerSourceManager(YoutubeAudioSourceManager())
        playerManager.registerSourceManager(TwitchStreamAudioSourceManager())

        ConnectorNativeLibLoader.loadConnectorLibrary()
    }

    override fun playInChannel(identifier: String, voiceChannel: VoiceChannel) {
        val guild = voiceChannel.guild
        val audioManager = guild.audioManager
        val audioPlayer = audioPlayers.getOrPut(guild.id, { createPlayer(guild) })

        if (audioManager.isAttemptingToConnect) {
            val queued = audioManager.queuedAudioConnection
            if (queued != null && queued.id != voiceChannel.id) {
                audioManager.closeAudioConnection()
            }
        }

        audioManager.openAudioConnection(voiceChannel)
        audioManager.sendingHandler = AudioPlayerSendHandler(audioPlayer)

        playerManager.loadItem(identifier, FunctionalResultHandler(
            {
                logger.debug("Playing track {} on channel #{}", it.info.identifier, voiceChannel.name)
                audioPlayer.playTrack(it)
            },
            {},
            { logger.error("Could not find track with identifier {}", identifier) },
            { e -> logger.error("Exception while trying to load track", e) }
        ))
    }

    override fun setVolume(guildId: String, volume: Int) {
        audioPlayers[guildId]?.volume = volume.coerceAtLeast(1).coerceAtMost(100)
    }

    override fun getVolume(guildId: String): Int? {
        return audioPlayers[guildId]?.volume
    }

    private fun createPlayer(guild: Guild): AudioPlayer {
        logger.debug("Creating new instance of AudioPlayer for Guild {} ({})", guild.name, guild.id)
        val player = playerManager.createPlayer()
        player.volume = 75
        return player
    }

    private class AudioPlayerSendHandler(private val audioPlayer: AudioPlayer) : AudioSendHandler {
        private var lastFrame: AudioFrame? = null

        override fun isOpus() = true
        override fun provide20MsAudio(): ByteBuffer = ByteBuffer.wrap(lastFrame?.data)
        override fun canProvide(): Boolean {
            lastFrame = audioPlayer.provide()
            return lastFrame != null
        }
    }
}

