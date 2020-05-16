package com.github.sipe90.sackbot.audio

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason
import reactor.util.function.Tuple2
import reactor.util.function.Tuples
import reactor.kotlin.core.util.function.component1
import reactor.kotlin.core.util.function.component2
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue

class TrackScheduler(val player: AudioPlayer, var defaultVolume: Int = 75) : AudioEventAdapter() {

    private val queue: BlockingQueue<Tuple2<AudioTrack, Int>> = LinkedBlockingQueue()

    /**
     * Add the next track to queue or play right away if nothing is in the queue.
     *
     * @param track The track to play or add to queue.
     */
    fun queue(track: AudioTrack, volume: Int?) {
        if (!player.startTrack(track, true)) {
            queue.offer(Tuples.of(track, volume ?: defaultVolume))
        }
    }

    fun interrupt(track: AudioTrack, volume: Int?) = interrupt(track, false, volume)

    fun interrupt(track: AudioTrack, preserveQueue: Boolean, volume: Int?) {
        player.volume = volume ?: defaultVolume
        player.playTrack(track)
        if (!preserveQueue) {
            queue.clear()
        }
    }

    /**
     * Start the next track, stopping the current one if it is playing.
     */
    fun nextTrack() {
        val next = queue.poll()
        if (next != null) {
            val (track, volume) = next
            player.volume = volume
            player.startTrack(track, false)
        }
    }

    override fun onTrackEnd(
        player: AudioPlayer,
        track: AudioTrack,
        endReason: AudioTrackEndReason
    ) {
        if (endReason.mayStartNext) {
            nextTrack()
        }
    }
}