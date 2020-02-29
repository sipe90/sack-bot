package com.github.sipe90.sackbot.component

import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import java.io.IOException
import java.nio.file.*
import java.nio.file.StandardWatchEventKinds.*

@Component
class FileWatcher {

    private val logger = LoggerFactory.getLogger(javaClass)

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
                            logger.debug("Path {} points to a folder, ignoring watch event")
                            return
                        }

                        when (event.kind()) {
                            ENTRY_CREATE -> onFileCreate(filePath)
                            ENTRY_MODIFY -> onFileModify(filePath)
                            ENTRY_DELETE -> onFileDelete(filePath)
                        }
                    }

                    if (!key.reset()) {
                        logger.info("Watch key is no longer valid. Stopping file watcher")
                        break
                    }
                }
            }
        } catch (e: IOException) {
            logger.error("Could not start file watcher", e)
        } catch (e: ClosedWatchServiceException) {
            logger.info("File watcher service has closed. Stopping file watcher")
        }
    }

    fun stop() {
        watchService.close()
    }

    private fun onFileCreate(filePath: Path) {
        logger.debug("File {} created", filePath)
    }

    private fun onFileDelete(filePath: Path) {
        logger.debug("File {} deleted", filePath)
    }

    private fun onFileModify(filePath: Path) {
        logger.debug("File {} modified", filePath)
    }
}