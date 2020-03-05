package com.github.sipe90.sackbot.service

import club.minnced.jda.reactor.toMono
import com.github.sipe90.sackbot.component.FileWatcher
import com.github.sipe90.sackbot.config.FilesConfig
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

@Service
class AudioFileServiceImpl(config: FilesConfig, watcher: FileWatcher) : AudioFileService {

    private final val logger = LoggerFactory.getLogger(javaClass)

    private final val audioFolderPath: Path = Paths.get(config.folder)

    init {
        if (Files.notExists(audioFolderPath)) {
            logger.info("Folder path {} not found, attempting to auto-create...", audioFolderPath)
            try {
                Files.createDirectories(audioFolderPath)
            } catch (e: IOException) {
                logger.error("Failed to create folders", e)
            }
            logger.info("Folder successfully created")
        }

        if (!Files.isReadable(audioFolderPath)) {
            throw IllegalArgumentException("Audio folder path " + config.folder + " is not readable")
        }

        if (!Files.isDirectory(audioFolderPath)) {
            throw IllegalArgumentException("Audio folder path " + config.folder + " does not point to a directory")
        }

        if (config.watcher.enabled) {
            watcher.start(audioFolderPath)
        }
    }

    override fun getAudioFilePaths(): Flux<Path> {
        return Flux.fromStream {
            Files.list(audioFolderPath).filter { Files.isRegularFile(it) && Files.isReadable(it) }
        }
    }

    override fun getAudioFilePathByName(name: String): Mono<Path> {
        return getAudioFilePaths().filter { stripExtension(it.fileName.toString()) == name }.next()
    }

    override fun audioFileExists(name: String): Mono<Boolean> {
        return getAudioFilePathByName(name).map { true }.defaultIfEmpty(false)
    }

    override fun getAudioFiles(): Flux<String> {
        return getAudioFilePaths().map { stripExtension(it.fileName.toString()) }
    }

    override fun saveAudioFile(name: String, inputStream: InputStream): Mono<Path> {
        return File(audioFolderPath.toString(), name).outputStream().use {
            val buf = ByteArray(1024)
            var count = 0
            while (inputStream.read(buf).also { b -> count = b } > 0) {
                it.write(buf, 0, count)
            }
        }.toMono()
            .map { audioFolderPath.resolve(name) }
            .doOnSuccess { logger.debug("Saved audio file to $it") }
    }

    fun stripExtension(fileName: String): String {
        val last = fileName.lastIndexOf('.')
        return if (last > 0) fileName.substring(0, last) else fileName
    }
}