package com.github.sipe90.sackbot.component

import mu.KotlinLogging
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import java.io.IOException
import java.nio.file.ClosedWatchServiceException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardWatchEventKinds.ENTRY_CREATE
import java.nio.file.StandardWatchEventKinds.ENTRY_DELETE
import java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY
import java.nio.file.WatchEvent
import java.nio.file.WatchService

@Component
class FileWatcher {

    private val logger = KotlinLogging.logger {}

    private lateinit var watchService: WatchService

    @Async
    fun start(path: Path) {
        logger.info("Starting file watcher service")
        try {
            watchService = path.fileSystem.newWatchService()
            watchService.use {
                path.register(watchService, ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE)
                logger.info("Watching folder {} for changes...", path)
                while (true) {
                    val key = watchService.take() ?: break
                    key.pollEvents().forEach {
                        @Suppress("UNCHECKED_CAST")
                        val event = it as WatchEvent<Path>

                        val fileName = event.context()
                        val filePath = path.resolve(fileName)

                        if (Files.isDirectory(filePath)) {
                            logger.debug { "Path $filePath points to a folder, ignoring watch event" }
                            return
                        }

                        when (event.kind()) {
                            ENTRY_CREATE -> onFileCreate(filePath)
                            ENTRY_MODIFY -> onFileModify(filePath)
                            ENTRY_DELETE -> onFileDelete(filePath)
                        }
                    }

                    if (!key.reset()) {
                        logger.info { "Watch key is no longer valid. Stopping file watcher" }
                        break
                    }
                }
            }
        } catch (e: IOException) {
            logger.error(e) { "Could not start file watcher" }
        } catch (e: ClosedWatchServiceException) {
            logger.info { "File watcher service has closed. Stopping file watcher" }
        } catch (e: InterruptedException) {
            logger.info { "Thread interrupted. Stopping file watcher" }
        }
    }

    fun stop() {
        watchService.close()
    }

    private fun onFileCreate(filePath: Path) {
        logger.debug { "File $filePath created" }
    }

    private fun onFileDelete(filePath: Path) {
        logger.debug { "File $filePath deleted" }
    }

    private fun onFileModify(filePath: Path) {
        logger.debug { "File $filePath modified" }
    }
}
