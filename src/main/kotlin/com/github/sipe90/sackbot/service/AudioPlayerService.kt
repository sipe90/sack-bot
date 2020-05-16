package com.github.sipe90.sackbot.service

import net.dv8tion.jda.api.entities.VoiceChannel
import reactor.core.publisher.Mono

interface AudioPlayerService {

    fun playAudioInChannel(name: String, voiceChannel: VoiceChannel, volume: Int?): Mono<Void>

    fun playTtsInChannel(text: String, voiceChannel: VoiceChannel): Mono<Boolean>

    fun playTtsForUser(guildId: String, userId: String, text: String): Mono<Boolean>

    fun playRandomTtsInChannel(voiceChannel: VoiceChannel): Mono<Boolean>

    fun playRandomTtsForUser(guildId: String, userId: String): Mono<Boolean>

    fun setVolume(guildId: String, volume: Int)

    fun getVolume(guildId: String): Int?

    fun playUrlInChannel(url: String, voiceChannel: VoiceChannel, volume: Int?): Mono<Boolean>

    fun playAudioForUser(userId: String, name: String, volume: Int?): Mono<Void>

    fun playAudioForUser(guildId: String, userId: String, name: String, volume: Int?): Mono<Void>

    fun playVoiceLinesInChannel(voice: String, lines: List<String>, voiceChannel: VoiceChannel): Mono<Void>

    fun playVoiceLinesForUser(guildId: String, userId: String, voice: String, lines: List<String>): Mono<Void>
}