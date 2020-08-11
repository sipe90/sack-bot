package com.github.sipe90.sackbot.service

import com.github.sipe90.sackbot.audio.ByteArraySeekableInputStream
import com.github.sipe90.sackbot.exception.ValidationException
import com.github.sipe90.sackbot.persistence.AudioFileRepository
import com.github.sipe90.sackbot.persistence.dto.AudioFile
import com.github.sipe90.sackbot.util.withExtension
import com.sedmelluq.discord.lavaplayer.container.MediaContainer
import com.sedmelluq.discord.lavaplayer.container.MediaContainerDetection
import com.sedmelluq.discord.lavaplayer.container.MediaContainerHints
import com.sedmelluq.discord.lavaplayer.container.MediaContainerRegistry
import com.sedmelluq.discord.lavaplayer.track.AudioReference
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.io.ByteArrayOutputStream
import java.time.Instant
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

@Service
class AudioFileServiceImpl(private val audioFileRepository: AudioFileRepository) : AudioFileService {

    private final val logger = LoggerFactory.getLogger(javaClass)

    private final val containerRegistry = MediaContainerRegistry(
            listOf(
                    MediaContainer.MP3.probe,
                    MediaContainer.OGG.probe,
                    MediaContainer.WAV.probe
            ))

    override fun findAudioFile(guildId: String, name: String): Mono<AudioFile> {
        return audioFileRepository.findOne(guildId, name)
    }

    override fun randomAudioFile(guildId: String, userId: String, tags: Set<String>): Mono<AudioFile> =
            if (tags.isEmpty()) randomAudioFile(guildId, userId)
            else randomAudioFile(getAudioFiles(guildId, userId).filter { it.tags.containsAll(tags) })

    override fun randomAudioFile(guildId: String, userId: String): Mono<AudioFile> =
            randomAudioFile(getAudioFiles(guildId, userId))

    private fun randomAudioFile(audioFiles: Flux<AudioFile>): Mono<AudioFile> =
            audioFiles.count()
                    .map { (1..it).random() }
                    .flatMap { audioFiles.take(it).last() }

    override fun zipFiles(guildId: String, userId: String): Mono<ByteArray> =
            getAudioFiles(guildId, userId)
                    .collectList()
                    .map {
                        ByteArrayOutputStream().use { baos ->
                            val zip = ZipOutputStream(baos)
                            it.forEach {
                                val entry = ZipEntry(withExtension(it.name, it.extension))
                                zip.putNextEntry(entry)
                                zip.write(it.data)
                                zip.closeEntry()
                            }
                            zip.finish()
                            baos
                        }.toByteArray()
                    }

    override fun audioFileExists(guildId: String, name: String): Mono<Boolean> {
        return findAudioFile(guildId, name).map { true }.defaultIfEmpty(false)
    }

    override fun getAudioFiles(guildId: String, userId: String): Flux<AudioFile> {
        return audioFileRepository.getAllAudioFiles(guildId)
    }

    override fun saveAudioFile(
            guildId: String,
            name: String,
            extension: String?,
            tags: Set<String>,
            data: ByteArray,
            userId: String
    ): Mono<AudioFile> =
            validateFile(data, name, extension).then(
                    audioFileRepository.saveAudioFile(
                            AudioFile(
                                    name,
                                    extension,
                                    data.size,
                                    guildId,
                                    tags,
                                    userId,
                                    Instant.now(),
                                    null,
                                    null,
                                    data
                            )
                    )
            )

    override fun updateAudioFile(
            guildId: String,
            name: String,
            audioFile: AudioFile,
            userId: String
    ): Mono<Boolean> = validateFile(audioFile.data, audioFile.name, audioFile.extension).then(
            Mono.defer {
                if (guildId != audioFile.guildId) throw ValidationException("Guild id cannot be updated")
                audioFile.modified = Instant.now()
                audioFile.modifiedBy = userId
                audioFileRepository.updateAudioFile(guildId, name, audioFile)
            }
    )

    override fun deleteAudioFile(guildId: String, name: String): Mono<Boolean> {
        return audioFileRepository.deleteAudioFile(guildId, name)
    }

    private fun validateFile(data: ByteArray, name: String, extension: String?): Mono<Void> {
        val detectionResult = ByteArraySeekableInputStream(data).use { inputStream ->
            MediaContainerDetection(
                    containerRegistry,
                    AudioReference("$name${if (extension !== null) ".$extension" else ""}", null),
                    inputStream,
                    MediaContainerHints.from(null, extension)
            ).detectContainer()
        }
        return if (!detectionResult.isContainerDetected) Mono.error(ValidationException("Invalid or unsupported sound file format")) else Mono.empty()
    }
}