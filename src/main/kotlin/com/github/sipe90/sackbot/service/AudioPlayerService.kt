package com.github.sipe90.sackbot.service

import net.dv8tion.jda.api.entities.VoiceChannel
import reactor.core.publisher.Mono

interface AudioPlayerService {

    fun playAudioInChannel(name: String, voiceChannel: VoiceChannel): Mono<Unit>

    fun playTtsInChannel(text: String, voiceChannel: VoiceChannel): Mono<Unit>

    fun playRandomTtsInChannel(voiceChannel: VoiceChannel): Mono<Unit>

    fun setVolume(guildId: String, volume: Int)

    fun getVolume(guildId: String): Int?

    fun playUrlInChannel(url: String, voiceChannel: VoiceChannel): Mono<Boolean>

    fun playAudioForUser(userId: String, name: String): Mono<Unit>

    fun playAudioForUser(guildId: String, userId: String, name: String): Mono<Unit>

    fun playVoiceLinesInChannel(voice: String, lines: List<String>, voiceChannel: VoiceChannel): Mono<Unit>

    fun playVoiceLinesForUser(guildId: String, userId: String, voice: String, lines: List<String>): Mono<Unit>
}