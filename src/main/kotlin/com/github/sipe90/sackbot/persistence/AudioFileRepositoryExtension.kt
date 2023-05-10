package com.github.sipe90.sackbot.persistence

import com.github.sipe90.sackbot.persistence.dto.AudioFile
import com.github.sipe90.sackbot.persistence.dto.LightAudioFile
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface AudioFileRepositoryExtension {

    fun getAllAudioFilesWithoutData(guildId: String): Flux<LightAudioFile>

    fun audioFileExists(guildId: String, name: String): Mono<Boolean>

    fun findAudioFile(guildId: String, name: String): Mono<AudioFile>

    fun findAllAudioFiles(guildId: String): Flux<AudioFile>

    fun deleteAudioFile(guildId: String, name: String): Mono<Boolean>

    fun findRandomAudioFile(guildId: String, tags: Set<String>): Mono<AudioFile>
}
