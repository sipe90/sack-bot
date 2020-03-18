package com.github.sipe90.sackbot.service

import com.github.sipe90.sackbot.persistence.dto.AudioFile
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface AudioFileService {

    fun audioFileExists(guildId: String, name: String, userId: String): Mono<Boolean>

    fun getAudioFiles(guildId: String, userId: String): Flux<AudioFile>

    fun saveAudioFile(
        guildId: String,
        name: String,
        extension: String?,
        data: Flux<Byte>,
        userId: String
    ): Mono<AudioFile>

    fun updateAudioFile(audioFile: AudioFile, data: Flux<Byte>, userId: String): Mono<Boolean>

    fun findAudioFile(guildId: String, name: String): Mono<AudioFile>

    fun zipFiles(guildId: String, userId: String): Mono<ByteArray>
}