package com.github.sipe90.sackbot.handler

import com.github.sipe90.sackbot.auth.DiscordUser
import com.github.sipe90.sackbot.component.VoiceLines
import com.github.sipe90.sackbot.exception.ValidationException
import com.github.sipe90.sackbot.service.AudioPlayerService
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.ServerResponse.noContent
import org.springframework.web.reactive.function.server.ServerResponse.ok
import org.springframework.web.reactive.function.server.bodyToMono
import reactor.core.publisher.Mono

@Component
class VoiceHandler(private val voiceLines: VoiceLines, private val audioPlayerService: AudioPlayerService) {

    fun getVoiceLines(request: ServerRequest, principal: DiscordUser): Mono<ServerResponse> {
        return ok().bodyValue(voiceLines.getVoiceLines())
    }

    fun playVoiceLines(request: ServerRequest, principal: DiscordUser): Mono<ServerResponse> {
        val guildId = request.pathVariable("guildId")
        val userId = principal.getId()
        val voice = request.queryParam("voice").orElseThrow { ValidationException("Voice name is required") }

        return request.bodyToMono<List<String>>().flatMap {
            audioPlayerService.playVoiceLinesForUser(guildId, userId, voice, it)
        }.flatMap { noContent().build() }
    }
}