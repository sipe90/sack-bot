package com.github.sipe90.sackbot.service

import com.github.sipe90.sackbot.audio.NitriteAudioSourceManager
import com.github.sipe90.sackbot.component.Text2Speech
import com.sedmelluq.discord.lavaplayer.natives.ConnectorNativeLibLoader
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager
import com.sedmelluq.discord.lavaplayer.player.FunctionalResultHandler
import com.sedmelluq.discord.lavaplayer.source.beam.BeamAudioSourceManager
import com.sedmelluq.discord.lavaplayer.source.local.LocalAudioSourceManager
import com.sedmelluq.discord.lavaplayer.source.twitch.TwitchStreamAudioSourceManager
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager
import com.sedmelluq.discord.lavaplayer.track.playback.AudioFrame
import net.dv8tion.jda.api.audio.AudioSendHandler
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.VoiceChannel
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.nio.ByteBuffer

@Service
class AudioPlayerServiceImpl(private val tts: Text2Speech, nitriteManager: NitriteAudioSourceManager) :
    AudioPlayerService {

    private final val logger = LoggerFactory.getLogger(javaClass)

    private final val playerManager: AudioPlayerManager = DefaultAudioPlayerManager()
    private final val audioPlayers = mutableMapOf<String, AudioPlayer>()

    init {
        playerManager.registerSourceManager(nitriteManager)
        playerManager.registerSourceManager(LocalAudioSourceManager())
        playerManager.registerSourceManager(YoutubeAudioSourceManager())
        playerManager.registerSourceManager(TwitchStreamAudioSourceManager())
        playerManager.registerSourceManager(BeamAudioSourceManager())

        ConnectorNativeLibLoader.loadConnectorLibrary()
    }

    override fun playAudioInChannel(name: String, voiceChannel: VoiceChannel): Mono<Boolean> {
        return playInChannel("${voiceChannel.guild.id}:${name}", voiceChannel)
    }

    override fun playRandomTtsInChannel(voiceChannel: VoiceChannel): Mono<Boolean> {
        return tts.randomPhraseToSpeech().flatMap { playInChannel(it.toString(), voiceChannel) }
    }

    override fun playTtsInChannel(text: String, voiceChannel: VoiceChannel): Mono<Boolean> {
        return tts.textToSpeech(text).flatMap { playInChannel(it.toString(), voiceChannel) }
    }

    override fun playUrlInChannel(url: String, voiceChannel: VoiceChannel): Mono<Boolean> {
        return playInChannel(url, voiceChannel)
    }

    private fun playInChannel(identifier: String, voiceChannel: VoiceChannel): Mono<Boolean> {
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

        logger.debug("Looking for track with identifier: {}", identifier)

        return Mono.create<Boolean> { sink ->
            playerManager.loadItem(identifier, FunctionalResultHandler(
                {
                    logger.debug("Playing track {} on channel #{}", it.info.title, voiceChannel.name)
                    audioPlayer.playTrack(it)
                    sink.success(true)
                },
                {
                    logger.debug("Found playlist with tracks {}", it.tracks.map { track -> track.info.title })
                    // audioPlayer.playTrack(it)
                    sink.success(false)
                },
                {
                    logger.error("Could not find track with identifier {}", identifier)
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
        audioPlayers[guildId]?.volume = volume
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

