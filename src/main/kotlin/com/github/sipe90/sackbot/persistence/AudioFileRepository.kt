package com.github.sipe90.sackbot.persistence

import club.minnced.jda.reactor.toMono
import com.github.sipe90.sackbot.persistence.dto.AudioFile
import org.dizitart.kno2.filters.and
import org.dizitart.kno2.filters.eq
import org.dizitart.no2.objects.ObjectRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.toFlux

@Repository
class AudioFileRepository(val repository: ObjectRepository<AudioFile>) {

    fun findOne(guildId: String, name: String): Mono<AudioFile> = Mono.defer {
        repository.find((AudioFile::guildId eq guildId) and (AudioFile::name eq name)).toMono()
            .flatMap {
                val file = it.firstOrDefault()
                return@flatMap if (file == null) Mono.empty<AudioFile>() else Mono.just(file)
            }
    }

    fun getAll(guildId: String): Flux<AudioFile> =
        repository.find(AudioFile::guildId eq guildId).sortedBy { it.name }.toFlux()

    fun saveAudioFile(audioFile: AudioFile): Mono<AudioFile> = Mono.defer {
        repository.insert(audioFile)
        Mono.just(audioFile)
    }

    fun updateAudioFile(audioFile: AudioFile): Mono<Boolean> = Mono.defer {
        (repository.update(
            findMemberFilter(audioFile.guildId, audioFile.name),
            audioFile,
            false
        ).affectedCount > 0).toMono()
    }

    private fun findMemberFilter(guildId: String, name: String) =
        (AudioFile::guildId eq guildId) and (AudioFile::name eq name)
}