package com.github.sipe90.sackbot.component

import com.github.sipe90.sackbot.audio.DatabaseAudioSourceManager
import com.github.sipe90.sackbot.audio.TrackScheduler
import com.github.sipe90.sackbot.audio.event.GuildVoiceEventEmitter
import com.github.sipe90.sackbot.exception.NotFoundException
import com.github.sipe90.sackbot.util.Initiator
import com.github.sipe90.sackbot.util.withContext
import com.sedmelluq.discord.lavaplayer.natives.ConnectorNativeLibLoader
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager
import com.sedmelluq.discord.lavaplayer.player.FunctionalResultHandler
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter
import com.sedmelluq.discord.lavaplayer.source.bandcamp.BandcampAudioSourceManager
import com.sedmelluq.discord.lavaplayer.source.getyarn.GetyarnAudioSourceManager
import com.sedmelluq.discord.lavaplayer.source.http.HttpAudioSourceManager
import com.sedmelluq.discord.lavaplayer.source.twitch.TwitchStreamAudioSourceManager
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager
import com.sedmelluq.discord.lavaplayer.track.AudioItem
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioReference
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason
import kotlinx.coroutines.reactor.mono
import mu.KotlinLogging
import net.dv8tion.jda.api.audio.AudioSendHandler
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import java.time.Duration

@Component
class LavaPlayerManager(private val dbSourceManager: DatabaseAudioSourceManager, private val voiceEventEmitter: GuildVoiceEventEmitter) {

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

    fun playDatabaseTrack(trackName: String, audioChannel: AudioChannel): Mono<AudioTrack> {
        val guild = audioChannel.guild
        val trackScheduler = getScheduler(guild)

        val currentChannel = guild.selfMember.voiceState?.channel
        val identifier = "${audioChannel.guild.id}:$trackName"

        return withContext { initiator ->
            connectIfRequired(currentChannel, audioChannel, trackScheduler, initiator)
                .then(
                    mono {
                        val track = dbSourceManager.loadItem(playerManager, AudioReference(identifier, trackName)) as AudioTrack?
                        if (track != null) {
                            track.userData = initiator
                        }
                        track
                    },
                ).doOnNext { trackScheduler.interrupt(it!!) }
        }
    }

    fun playExternalTrack(identifier: String, audioChannel: AudioChannel): Mono<AudioItem> {
        val guild = audioChannel.guild
        val trackScheduler = getScheduler(guild)

        val currentChannel = guild.selfMember.voiceState?.channel

        return withContext { initiator ->
            connectIfRequired(currentChannel, audioChannel, trackScheduler, initiator)
                .then(loadExternalTrack(identifier, initiator))
                .doOnSuccess {
                    if (it is AudioTrack) {
                        trackScheduler.interrupt(it)
                    } else if (it is AudioPlaylist) {
                        trackScheduler.interrupt(it)
                    }
                }
        }
    }

    private fun loadExternalTrack(identifier: String, initiator: Initiator?): Mono<AudioItem> =
        Mono.create { sink ->
            playerManager.loadItem(
                identifier,
                FunctionalResultHandler(
                    { track ->
                        logger.debug { "Found external track ${track.info.title}" }
                        track.userData = initiator
                        sink.success(track)
                    },
                    { playlist ->
                        logger.debug {
                            "Found external playlist with tracks ${playlist.tracks.map { track -> track.info.title }}"
                        }
                        playlist.tracks.forEach { track -> track.userData = initiator }
                        sink.success(playlist)
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
            audioPlayer.addListener(AudioEventListener(guild.id, voiceEventEmitter))
            scheduler
        }
    }

    private fun connectIfRequired(
        currentChannel: AudioChannelUnion?,
        audioChannel: AudioChannel,
        trackScheduler: TrackScheduler,
        initiator: Initiator?,
    ): Mono<Void> =
        if (currentChannel != audioChannel) {
            connect(audioChannel, trackScheduler.sendHandler)
                .doOnSuccess {
                    voiceEventEmitter.onVoiceChannelChange(audioChannel.guild.id, initiator, currentChannel?.name, audioChannel.name)
                }
        } else {
            Mono.empty()
        }

    private fun connect(audioChannel: AudioChannel, sendHandler: AudioSendHandler): Mono<Void> {
        val audioManager = audioChannel.guild.audioManager
        audioManager.openAudioConnection(audioChannel)
        audioManager.sendingHandler = sendHandler

        return Mono.delay(Duration.ofMillis(100)).then()
    }

    fun setVolume(guild: Guild, volume: Int): Mono<Unit> {
        return withContext { initiator ->
            val scheduler = getScheduler(guild)
            logger.debug { "Setting volume for guild ${guild.id} player to $volume" }
            scheduler.setVolume(volume)
            voiceEventEmitter.onVolumeChange(guild.id, initiator, volume)
            Mono.empty()
        }
    }

    fun getVolume(guild: Guild): Int {
        val scheduler = getScheduler(guild)
        return scheduler.getVolume()
    }

    class AudioEventListener(private val guildId: String, private val voiceEventEmitter: GuildVoiceEventEmitter) : AudioEventAdapter() {
        override fun onTrackStart(player: AudioPlayer, track: AudioTrack) {
            voiceEventEmitter.onTrackStart(guildId, track)
        }

        override fun onTrackEnd(player: AudioPlayer, track: AudioTrack, endReason: AudioTrackEndReason) {
            voiceEventEmitter.onTrackEnd(guildId, track)
        }
    }
}
