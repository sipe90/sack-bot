package com.github.sipe90.sackbot.service

import com.github.sipe90.sackbot.audio.ByteArraySeekableInputStream
import com.github.sipe90.sackbot.audio.ContainerRegistries
import com.github.sipe90.sackbot.exception.ValidationException
import com.github.sipe90.sackbot.persistence.AudioFileRepository
import com.github.sipe90.sackbot.persistence.dto.AudioFile
import com.github.sipe90.sackbot.persistence.dto.LightAudioFile
import com.github.sipe90.sackbot.util.withExtension
import com.sedmelluq.discord.lavaplayer.container.MediaContainerDetection
import com.sedmelluq.discord.lavaplayer.container.MediaContainerHints
import com.sedmelluq.discord.lavaplayer.track.AudioReference
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.io.ByteArrayOutputStream
import java.time.Instant
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

@Service
class AudioFileServiceImpl(private val audioFileRepository: AudioFileRepository) : AudioFileService {
    override fun findAudioFile(
        guildId: String,
        name: String,
    ): Mono<AudioFile> {
        return audioFileRepository.findAudioFile(guildId, name)
    }

    override fun randomAudioFile(
        guildId: String,
        userId: String,
        tags: Set<String>,
    ): Mono<AudioFile> {
        return audioFileRepository.findRandomAudioFile(guildId, tags)
    }

    override fun randomAudioFile(
        guildId: String,
        userId: String,
    ): Mono<AudioFile> {
        return randomAudioFile(guildId, userId, emptySet())
    }

    override fun zipFiles(
        guildId: String,
        userId: String,
    ): Mono<ByteArray> {
        return audioFileRepository.findAllAudioFiles(guildId)
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
    }

    override fun audioFileExists(
        guildId: String,
        name: String,
    ): Mono<Boolean> {
        return audioFileRepository.audioFileExists(guildId, name)
    }

    override fun getAudioFiles(guildId: String): Flux<LightAudioFile> {
        return audioFileRepository.getAllAudioFilesWithoutData(guildId)
    }

    override fun saveAudioFile(
        guildId: String,
        name: String,
        extension: String?,
        data: ByteArray,
        userId: String,
    ): Mono<AudioFile> {
        return validateFile(data, name, extension).then(
            audioFileRepository.save(
                AudioFile(
                    name,
                    extension,
                    data.size,
                    guildId,
                    userId,
                    data,
                ),
            ),
        )
    }

    override fun updateAudioFile(
        guildId: String,
        name: String,
        audioFile: AudioFile,
        userId: String,
    ): Mono<AudioFile> {
        return validateFile(audioFile.data, audioFile.name, audioFile.extension).then(
            Mono.defer {
                if (guildId != audioFile.guildId) throw ValidationException("Guild id cannot be updated")
                audioFile.modified = Instant.now()
                audioFile.modifiedBy = userId
                audioFileRepository.save(audioFile)
            },
        )
    }

    override fun deleteAudioFile(
        guildId: String,
        name: String,
    ): Mono<Boolean> {
        return audioFileRepository.deleteAudioFile(guildId, name)
    }

    private fun validateFile(
        data: ByteArray,
        name: String,
        extension: String?,
    ): Mono<Void> {
        val detectionResult =
            ByteArraySeekableInputStream(data).use { inputStream ->
                MediaContainerDetection(
                    ContainerRegistries.audio,
                    AudioReference("$name${if (extension !== null) ".$extension" else ""}", null),
                    inputStream,
                    MediaContainerHints.from(null, extension),
                ).detectContainer()
            }
        return if (!detectionResult.isContainerDetected) {
            Mono.error(
                ValidationException("Invalid or unsupported sound file format"),
            )
        } else {
            Mono.empty()
        }
    }
}
