package com.github.sipe90.sackbot.service

import net.dv8tion.jda.api.entities.VoiceChannel
import reactor.core.publisher.Mono

interface AudioPlayerService {

    fun playInChannel(identifier: String, voiceChannel: VoiceChannel): Mono<Boolean>

    fun playTtsInChannel(text: String, voiceChannel: VoiceChannel): Mono<Boolean>

    fun setVolume(guildId: String, volume: Int)

    fun getVolume(guildId: String): Int?
}