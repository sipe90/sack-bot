package com.github.sipe90.sackbot.service

import com.sedmelluq.discord.lavaplayer.track.AudioItem
import net.dv8tion.jda.api.entities.VoiceChannel
import reactor.core.publisher.Mono

interface AudioPlayerService {

    fun playAudioForUser(guildId: String, userId: String, name: String, volume: Int?): Mono<Void>

    fun playAudioInChannel(name: String, voiceChannel: VoiceChannel, volume: Int?): Mono<Void>

    fun playUrlForUser(guildId: String, userId: String, url: String, volume: Int?): Mono<AudioItem>

    fun playUrlInChannel(url: String, voiceChannel: VoiceChannel, volume: Int?): Mono<AudioItem>

    fun setDefaultVolume(guildId: String, volume: Int)

    fun getDefaultVolume(guildId: String): Int?

}