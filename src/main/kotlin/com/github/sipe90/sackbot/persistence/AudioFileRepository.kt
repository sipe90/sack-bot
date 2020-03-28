package com.github.sipe90.sackbot.persistence

import com.github.sipe90.sackbot.persistence.dto.AudioFile
import org.dizitart.kno2.filters.and
import org.dizitart.kno2.filters.eq
import org.dizitart.no2.objects.ObjectRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toFlux
import reactor.kotlin.core.publisher.toMono

@Repository
class AudioFileRepository(val repository: ObjectRepository<AudioFile>) {

    fun findOne(guildId: String, name: String): Mono<AudioFile> = Mono.defer {
        repository.find((AudioFile::guildId eq guildId) and (AudioFile::name eq name)).toMono()
            .flatMap {
                val file = it.firstOrDefault()
                return@flatMap if (file == null) Mono.empty() else Mono.just(file)
            }
    }

    fun getAllAudioFiles(guildId: String): Flux<AudioFile> =
        repository.find(AudioFile::guildId eq guildId).sortedBy { it.name }.toFlux()

    fun saveAudioFile(audioFile: AudioFile): Mono<AudioFile> = Mono.defer {
        repository.insert(audioFile)
        Mono.just(audioFile)
    }

    fun updateAudioFile(guildId: String, name: String, audioFile: AudioFile): Mono<Boolean> = Mono.fromCallable {
        repository.update(
            findAudioFileFilter(guildId, name),
            audioFile,
            false
        ).affectedCount > 0
    }

    fun deleteAudioFile(guildId: String, name: String) = Mono.fromCallable {
        repository.remove(findAudioFileFilter(guildId, name)).affectedCount > 0
    }

    private fun findAudioFileFilter(guildId: String, name: String) =
        (AudioFile::guildId eq guildId) and (AudioFile::name eq name)
}