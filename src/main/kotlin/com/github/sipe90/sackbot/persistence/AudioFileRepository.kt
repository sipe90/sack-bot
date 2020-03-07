package com.github.sipe90.sackbot.persistence

import club.minnced.jda.reactor.toMono
import com.github.sipe90.sackbot.persistence.dto.AudioFile
import org.dizitart.kno2.filters.and
import org.dizitart.kno2.filters.eq
import org.dizitart.no2.objects.ObjectRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
class AudioFileRepository(val repository: ObjectRepository<AudioFile>) {

    fun findOne(guildId: String, name: String): Mono<AudioFile> = Mono.defer {
        repository.find((AudioFile::guildId eq guildId) and (AudioFile::name eq name)).toMono()
            .flatMap {
                val file = it.firstOrDefault()
                return@flatMap if (file == null) Mono.empty<AudioFile>() else Mono.just(file)
            }
    }

    fun getAll(guildId: String): Flux<AudioFile> = Flux.from { repository.find(AudioFile::guildId eq guildId) }

    fun saveAudioFile(audioFile: AudioFile): Mono<AudioFile> = Mono.defer {
        repository.insert(audioFile)
        Mono.just(audioFile)
    }
}