package com.github.sipe90.sackbot.service

import com.sedmelluq.discord.lavaplayer.track.AudioItem
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel
import reactor.core.publisher.Mono

interface AudioPlayerService {
    fun playAudioForUser(
        guildId: String,
        userId: String,
        name: String,
    ): Mono<Unit>

    fun playAudioInChannel(
        name: String,
        audioChannel: AudioChannel,
    ): Mono<Unit>

    fun playUrlForUser(
        guildId: String,
        userId: String,
        url: String,
    ): Mono<AudioItem>

    fun playUrlInChannel(
        url: String,
        audioChannel: AudioChannel,
    ): Mono<AudioItem>

    fun setVolume(
        guildId: String,
        volume: Int,
    ): Mono<Unit>

    fun getVolume(guildId: String): Int
}
