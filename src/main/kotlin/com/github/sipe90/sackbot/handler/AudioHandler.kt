package com.github.sipe90.sackbot.handler

import com.github.sipe90.sackbot.auth.DiscordUser
import com.github.sipe90.sackbot.exception.ValidationException
import com.github.sipe90.sackbot.persistence.dto.API
import com.github.sipe90.sackbot.service.AudioFileService
import com.github.sipe90.sackbot.service.AudioPlayerService
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.codec.json.Jackson2CodecSupport
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.ServerResponse.noContent
import org.springframework.web.reactive.function.server.ServerResponse.ok
import org.springframework.web.reactive.function.server.body
import reactor.core.publisher.Mono

@Component
class AudioHandler(
    private val audioPlayerService: AudioPlayerService,
    private val audioFileService: AudioFileService
) {

    fun playSound(request: ServerRequest, principal: DiscordUser): Mono<ServerResponse> {
        val guildId = request.pathVariable("guildId")
        val name = request.queryParam("name").orElseThrow { ValidationException("Sound name is required") }
        val userId = principal.getId()

        return audioPlayerService.playAudioForUser(guildId, userId, name)
            .flatMap { noContent().build() }
    }

    fun playRandomSound(request: ServerRequest, principal: DiscordUser): Mono<ServerResponse> {
        val guildId = request.pathVariable("guildId")
        val userId = principal.getId()

        return audioFileService.randomAudioFile(guildId, userId)
            .flatMap { audioPlayerService.playAudioForUser(guildId, userId, it.name) }
            .flatMap { noContent().build() }
    }

    fun getSoundsList(request: ServerRequest, principal: DiscordUser): Mono<ServerResponse> {
        val guildId = request.pathVariable("guildId")
        val userId = principal.getId()

        return ok()
            .hint(Jackson2CodecSupport.JSON_VIEW_HINT, API::class.java)
            .body(audioFileService.getAudioFiles(guildId, userId))
    }

    fun exportSounds(request: ServerRequest, principal: DiscordUser): Mono<ServerResponse> {
        val guildId = request.pathVariable("guildId")
        val userId = principal.getId()

        return ok()
            .contentType(MediaType.valueOf("application/zip"))
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"export.zip\"")
            .body(audioFileService.zipFiles(guildId, userId))
    }
}