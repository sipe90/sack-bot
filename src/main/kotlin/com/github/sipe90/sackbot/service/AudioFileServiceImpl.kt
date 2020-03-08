package com.github.sipe90.sackbot.service

import com.github.sipe90.sackbot.persistence.AudioFileRepository
import com.github.sipe90.sackbot.persistence.dto.AudioFile
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Instant

@Service
class AudioFileServiceImpl(private val audioFileRepository: AudioFileRepository) : AudioFileService {

    private final val logger = LoggerFactory.getLogger(javaClass)

    override fun findAudioFile(guildId: String, name: String): Mono<AudioFile> {
        return audioFileRepository.findOne(guildId, name)
    }

    override fun audioFileExists(guildId: String, name: String): Mono<Boolean> {
        return findAudioFile(guildId, name).map { true }.defaultIfEmpty(false)
    }

    override fun getAudioFiles(guildId: String): Flux<AudioFile> = audioFileRepository.getAll(guildId)

    override fun saveAudioFile(
        guildId: String,
        name: String,
        extension: String?,
        bytes: Flux<Byte>,
        userId: String
    ): Mono<AudioFile> =
        bytes.collectList().flatMap {
            audioFileRepository.saveAudioFile(
                AudioFile(
                    name,
                    extension,
                    guildId,
                    userId,
                    Instant.now(),
                    it.toByteArray()
                )
            )
        }
}