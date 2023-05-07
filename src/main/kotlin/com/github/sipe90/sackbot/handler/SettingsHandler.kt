package com.github.sipe90.sackbot.handler

import com.github.sipe90.sackbot.auth.DiscordUser
import com.github.sipe90.sackbot.config.BotConfig
import com.github.sipe90.sackbot.handler.dto.SettingsDTO
import com.github.sipe90.sackbot.service.JDAService
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono

@Component
class SettingsHandler(private val config: BotConfig, private val jdaService: JDAService) {

    fun getSettings(request: ServerRequest, principal: DiscordUser): Mono<ServerResponse> {
        return ServerResponse.ok().bodyValue(
            SettingsDTO(
                SettingsDTO.UploadSettings(
                    config.upload.sizeLimit,
                    config.upload.overrideExisting,
                ),
                jdaService.getSelf().effectiveAvatarUrl,
            ),
        )
    }
}
