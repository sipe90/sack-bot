package com.github.sipe90.sackbot.service

import net.dv8tion.jda.api.entities.VoiceChannel
import reactor.core.publisher.Mono

interface AudioPlayerService {

    fun playAudioInChannel(name: String, voiceChannel: VoiceChannel): Mono<Void>

    fun playTtsInChannel(text: String, voiceChannel: VoiceChannel): Mono<Void>

    fun playRandomTtsInChannel(voiceChannel: VoiceChannel): Mono<Void>

    fun setVolume(guildId: String, volume: Int)

    fun getVolume(guildId: String): Int?

    fun playUrlInChannel(url: String, voiceChannel: VoiceChannel): Mono<Boolean>

    fun playAudioForUser(userId: String, name: String): Mono<Void>

    fun playAudioForUser(guildId: String, userId: String, name: String): Mono<Void>

    fun playVoiceLinesInChannel(voice: String, lines: List<String>, voiceChannel: VoiceChannel): Mono<Void>
}