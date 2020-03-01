package com.github.sipe90.sackbot.service

import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.nio.file.Path

interface AudioFileService {

    fun getAudioFilePaths(): Flux<Path>

    fun getAudioFilePathByName(name: String): Mono<Path>

    fun getAudioFiles(): Flux<String>
}