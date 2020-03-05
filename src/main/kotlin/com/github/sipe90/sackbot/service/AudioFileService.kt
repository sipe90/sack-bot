package com.github.sipe90.sackbot.service

import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.io.InputStream
import java.nio.file.Path

interface AudioFileService {

    fun getAudioFilePaths(): Flux<Path>

    fun getAudioFilePathByName(name: String): Mono<Path>

    fun audioFileExists(name: String): Mono<Boolean>

    fun getAudioFiles(): Flux<String>

    fun saveAudioFile(name: String, inputStream: InputStream): Mono<Path>
}