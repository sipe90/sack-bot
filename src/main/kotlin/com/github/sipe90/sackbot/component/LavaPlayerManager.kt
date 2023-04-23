package com.github.sipe90.sackbot.component

import club.minnced.jda.reactor.then
import com.github.sipe90.sackbot.audio.DatabaseAudioSourceManager
import com.github.sipe90.sackbot.audio.TrackScheduler
import com.github.sipe90.sackbot.audio.TrackSchedulerEvent
import com.github.sipe90.sackbot.exception.NotFoundException
import com.sedmelluq.discord.lavaplayer.natives.ConnectorNativeLibLoader
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager
import com.sedmelluq.discord.lavaplayer.player.FunctionalResultHandler
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManager
import com.sedmelluq.discord.lavaplayer.source.bandcamp.BandcampAudioSourceManager
import com.sedmelluq.discord.lavaplayer.source.getyarn.GetyarnAudioSourceManager
import com.sedmelluq.discord.lavaplayer.source.http.HttpAudioSourceManager
import com.sedmelluq.discord.lavaplayer.source.twitch.TwitchStreamAudioSourceManager
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager
import com.sedmelluq.discord.lavaplayer.track.AudioItem
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioReference
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import com.sedmelluq.discord.lavaplayer.track.playback.MutableAudioFrame
import kotlinx.coroutines.reactor.mono
import mu.KotlinLogging
import net.dv8tion.jda.api.audio.AudioSendHandler
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.nio.ByteBuffer
import java.time.Duration

@Component
class LavaPlayerManager(private val dbSourceManager: DatabaseAudioSourceManager) {

    private val logger = KotlinLogging.logger {}

    private final val playerManager = DefaultAudioPlayerManager()
    private final val trackSchedulers = mutableMapOf<String, TrackScheduler>()

    init {
        playerManager.registerSourceManager(YoutubeAudioSourceManager())
        playerManager.registerSourceManager(TwitchStreamAudioSourceManager())
        playerManager.registerSourceManager(BandcampAudioSourceManager())
        playerManager.registerSourceManager(GetyarnAudioSourceManager())
        playerManager.registerSourceManager(HttpAudioSourceManager())

        ConnectorNativeLibLoader.loadConnectorLibrary()
    }

    fun playDatabaseTrack(identifier: String, audioChannel: AudioChannel): Mono<AudioTrack> {
        return playTrack(dbSourceManager, identifier, audioChannel)
    }

    private fun playTrack(
        sourceManager: AudioSourceManager,
        identifier: String,
        audioChannel: AudioChannel,
    ): Mono<AudioTrack> {
        return Mono.defer {
            val guild = audioChannel.guild
            val trackScheduler = getScheduler(guild)

            connect(audioChannel, trackScheduler.player).then {
                mono {
                    sourceManager.loadItem(playerManager, AudioReference(identifier, null)) as AudioTrack?
                }
            }.doOnNext { trackScheduler.interrupt(it!!) }
        }
    }

    fun playExternalTrack(identifier: String, audioChannel: AudioChannel): Mono<AudioItem> {
        return Mono.defer {
            val guild = audioChannel.guild
            val trackScheduler = getScheduler(guild)

            connect(audioChannel, trackScheduler.player).then {
                loadExternalTrack(identifier)
            }.doOnSuccess {
                if (it is AudioTrack) {
                    trackScheduler.interrupt(it)
                } else if (it is AudioPlaylist) trackScheduler.interrupt(it)
            }
        }
    }

    private fun loadExternalTrack(identifier: String): Mono<AudioItem> =
        Mono.create { sink ->
            playerManager.loadItem(
                identifier,
                FunctionalResultHandler(
                    {
                        logger.debug { "Found external track ${it.info.title}" }
                        sink.success(it)
                    },
                    {
                        logger.debug {
                            "Found external playlist with tracks ${it.tracks.map { track -> track.info.title }}"
                        }
                        sink.success(it)
                    },
                    {
                        logger.warn { "Could not find external track with identifier $identifier" }
                        sink.error(NotFoundException("No external track found"))
                    },
                    { e ->
                        logger.error(e) { "Exception while trying to external load track" }
                        sink.error(e)
                    },
                ),
            )
        }

    private fun getScheduler(guild: Guild): TrackScheduler {
        return trackSchedulers.getOrPut(guild.id) {
            logger.debug { "Creating new instance of TrackScheduler for Guild ${guild.name} (${guild.id})" }
            val audioPlayer = playerManager.createPlayer()
            val scheduler = TrackScheduler(audioPlayer)
            audioPlayer.addListener(scheduler)
            scheduler
        }
    }

    private fun connect(audioChannel: AudioChannel, audioPlayer: AudioPlayer): Mono<Void> {
        return Mono.defer {
            val audioManager = audioChannel.guild.audioManager
            audioManager.openAudioConnection(audioChannel)
            audioManager.sendingHandler = AudioPlayerSendHandler(audioPlayer)
            Mono.delay(Duration.ofMillis(100)).then()
        }
    }

    fun setVolume(guild: Guild, volume: Int) {
        val scheduler = getScheduler(guild)
        logger.debug { "Setting volume for guild ${guild.id} player to $volume" }
        scheduler.setVolume(volume)
    }

    fun getVolume(guild: Guild): Int {
        val scheduler = getScheduler(guild)
        return scheduler.getVolume()
    }

    fun onTrackEvent(guild: Guild): Flux<TrackSchedulerEvent> {
        val scheduler = getScheduler(guild)
        return scheduler.onTrackSchedulerEvent()
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
