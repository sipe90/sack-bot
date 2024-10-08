package com.github.sipe90.sackbot.service

import com.github.sipe90.sackbot.persistence.dto.AudioFile
import com.github.sipe90.sackbot.persistence.dto.LightAudioFile
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface AudioFileService {
    fun audioFileExists(
        guildId: String,
        name: String,
    ): Mono<Boolean>

    fun getAudioFiles(guildId: String): Flux<LightAudioFile>

    fun saveAudioFile(
        guildId: String,
        name: String,
        extension: String?,
        data: ByteArray,
        userId: String,
    ): Mono<AudioFile>

    fun updateAudioFile(
        guildId: String,
        name: String,
        audioFile: AudioFile,
        userId: String,
    ): Mono<AudioFile>

    fun findAudioFile(
        guildId: String,
        name: String,
    ): Mono<AudioFile>

    fun deleteAudioFile(
        guildId: String,
        name: String,
    ): Mono<Boolean>

    fun randomAudioFile(
        guildId: String,
        userId: String,
        tags: Set<String>,
    ): Mono<AudioFile>

    fun randomAudioFile(
        guildId: String,
        userId: String,
    ): Mono<AudioFile>

    fun zipFiles(
        guildId: String,
        userId: String,
    ): Mono<ByteArray>
}
