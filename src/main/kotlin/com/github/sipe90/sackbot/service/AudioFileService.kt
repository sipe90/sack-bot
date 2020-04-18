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
        tags: Set<String>,
        data: ByteArray,
        userId: String
    ): Mono<AudioFile>

    fun updateAudioFile(
        guildId: String,
        name: String,
        audioFile: AudioFile,
        userId: String
    ): Mono<Boolean>

    fun findAudioFile(guildId: String, name: String): Mono<AudioFile>

    fun deleteAudioFile(guildId: String, name: String): Mono<Boolean>

    fun randomAudioFile(guildId: String, userId: String, tags: Set<String>): Mono<AudioFile>

    fun randomAudioFile(guildId: String, userId: String): Mono<AudioFile>

    fun zipFiles(guildId: String, userId: String): Mono<ByteArray>
}