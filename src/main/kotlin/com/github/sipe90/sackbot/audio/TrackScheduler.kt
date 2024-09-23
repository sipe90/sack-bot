package com.github.sipe90.sackbot.audio

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason
import com.sedmelluq.discord.lavaplayer.track.playback.MutableAudioFrame
import net.dv8tion.jda.api.audio.AudioSendHandler
import java.nio.ByteBuffer
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue

class TrackScheduler(private val player: AudioPlayer) : AudioEventAdapter() {
    private val queue: BlockingQueue<AudioTrack> = LinkedBlockingQueue()

    val sendHandler: AudioSendHandler

    init {
        sendHandler = AudioPlayerSendHandler(player)
        player.volume = 75
    }

    fun setVolume(volume: Int) {
        player.volume = volume
    }

    fun getVolume(): Int {
        return player.volume
    }

    /**
     * Add the next track to queue or play right away if nothing is in the queue.
     *
     * @param track The track to play or add to queue.
     */
    fun queue(track: AudioTrack) =
        synchronized(queue) {
            if (!player.startTrack(track, true)) {
                queue.offer(track)
            }
        }

    fun queue(playlist: AudioPlaylist) =
        synchronized(queue) {
            for (track in playlist.tracks) {
                if (playlist.selectedTrack != null && track != playlist.selectedTrack) continue
                queue(track)
            }
        }

    fun interrupt(track: AudioTrack) = interrupt(track, false)

    fun interrupt(
        track: AudioTrack,
        preserveQueue: Boolean,
    ) = synchronized(queue) {
        player.playTrack(track)
        if (!preserveQueue) {
            queue.clear()
        }
    }

    fun interrupt(playlist: AudioPlaylist) =
        synchronized(queue) {
            var first = true
            for (track in playlist.tracks) {
                if (playlist.selectedTrack != null && track != playlist.selectedTrack) continue
                if (first) {
                    interrupt(track)
                    queue.clear()
                    first = false
                } else {
                    queue(track)
                }
            }
        }

    /**
     * Start the next track, stopping the current one if it is playing.
     */
    fun nextTrack() {
        val next = queue.poll()
        if (next != null) {
            player.startTrack(next, false)
        }
    }

    override fun onTrackEnd(
        player: AudioPlayer,
        track: AudioTrack,
        endReason: AudioTrackEndReason,
    ) {
        if (endReason.mayStartNext) {
            nextTrack()
        }
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
