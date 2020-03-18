package com.github.sipe90.sackbot.service

import com.github.sipe90.sackbot.exception.ValidationException
import com.github.sipe90.sackbot.persistence.AudioFileRepository
import com.github.sipe90.sackbot.persistence.dto.AudioFile
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.io.ByteArrayOutputStream
import java.time.Instant
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

@Service
class AudioFileServiceImpl(private val audioFileRepository: AudioFileRepository, private val jdaService: JDAService) :
    AudioFileService {

    private final val logger = LoggerFactory.getLogger(javaClass)

    override fun findAudioFile(guildId: String, name: String): Mono<AudioFile> {
        return audioFileRepository.findOne(guildId, name)
    }

    override fun zipFiles(guildId: String, userId: String): Mono<ByteArray> =
        getAudioFiles(guildId, userId)
            .collectList()
            .map {
                ByteArrayOutputStream().use { baos ->
                    val zip = ZipOutputStream(baos)
                    it.forEach {
                        val entry = ZipEntry(entryName(it.name, it.extension))
                        zip.putNextEntry(entry)
                        zip.write(it.data)
                        zip.closeEntry()
                    }
                    zip.finish()
                    baos
                }.toByteArray()
            }

    private fun entryName(fileName: String, extension: String?): String {
        if (extension == null) return fileName
        return "${fileName}.${extension}"
    }

    override fun audioFileExists(guildId: String, name: String, userId: String): Mono<Boolean> {
        validateGuild(guildId, userId)
        return findAudioFile(guildId, name).map { true }.defaultIfEmpty(false)
    }

    override fun getAudioFiles(guildId: String, userId: String): Flux<AudioFile> {
        validateGuild(guildId, userId)
        return audioFileRepository.getAll(guildId)
    }

    override fun saveAudioFile(
        guildId: String,
        name: String,
        extension: String?,
        data: Flux<Byte>,
        userId: String
    ): Mono<AudioFile> =
        data.collectList().flatMap {
            audioFileRepository.saveAudioFile(
                AudioFile(
                    name,
                    extension,
                    it.size,
                    guildId,
                    userId,
                    Instant.now(),
                    null,
                    null,
                    it.toByteArray()
                )
            )
        }

    override fun updateAudioFile(audioFile: AudioFile, data: Flux<Byte>, userId: String): Mono<Boolean> =
        data.collectList().flatMap {
            audioFile.data = it.toByteArray()
            audioFile.size = it.size
            audioFile.modified = Instant.now()
            audioFile.modifiedBy = userId
            audioFileRepository.updateAudioFile(audioFile)
        }

    private fun validateGuild(guildId: String, userId: String) {
        if (!jdaService.isMutualGuild(guildId, userId)) throw ValidationException("Invalid guild Id")
    }
}