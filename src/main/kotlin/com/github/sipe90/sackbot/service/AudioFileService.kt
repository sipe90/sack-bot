package com.github.sipe90.sackbot.service

import com.github.sipe90.sackbot.persistence.dto.AudioFile
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface AudioFileService {

    fun audioFileExists(guildId: String, name: String): Mono<Boolean>

    fun getAudioFiles(guildId: String): Flux<AudioFile>

    fun saveAudioFile(
        guildId: String,
        name: String,
        extension: String?,
        bytes: Flux<Byte>,
        userId: String
    ): Mono<AudioFile>

    fun findAudioFile(guildId: String, name: String): Mono<AudioFile>
}