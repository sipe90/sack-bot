package com.github.sipe90.sackbot.handler

import com.github.sipe90.sackbot.auth.DiscordUser
import com.github.sipe90.sackbot.exception.BadRequestException
import com.github.sipe90.sackbot.service.AudioPlayerService
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.ServerResponse.noContent
import org.springframework.web.reactive.function.server.ServerResponse.ok
import org.springframework.web.reactive.function.server.bodyToMono
import reactor.core.publisher.Mono

@Component
class TTSHandler(private val audioPlayerService: AudioPlayerService) {

    fun getAvailableVoices(request: ServerRequest, principal: DiscordUser): Mono<ServerResponse> {
        return ok().bodyValue(audioPlayerService.getAvailableVoices())
    }

    fun playTTS(request: ServerRequest, principal: DiscordUser): Mono<ServerResponse> {
        val guildId = request.pathVariable("guildId")
        val userId = principal.getId()
        val voice = getVoice(request)

        return request.bodyToMono<String>().flatMap {
            audioPlayerService.playTtsForUser(guildId, userId, voice, it)
        }.flatMap { noContent().build() }
    }

    fun playRandomTTS(request: ServerRequest, principal: DiscordUser): Mono<ServerResponse> {
        val guildId = request.pathVariable("guildId")
        val userId = principal.getId()
        val voice = getVoice(request)

        return audioPlayerService.playRandomTtsForUser(guildId, userId, voice)
                .flatMap { noContent().build() }
    }

    private fun getVoice(request: ServerRequest) = request.queryParam("voice").orElseThrow { BadRequestException("Query parameter url is required") }
}