package com.github.sipe90.sackbot.service

import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.nio.file.Path

interface AudioFileService {

    fun getAudioFiles(): Flux<Path>

    fun getAudioFileByName(name: String): Mono<Path>
}