package com.github.sipe90.sackbot.handler

import com.github.sipe90.sackbot.auth.DiscordUser
import com.github.sipe90.sackbot.component.TTS
import com.github.sipe90.sackbot.component.VoiceLines
import com.github.sipe90.sackbot.config.BotConfig
import com.github.sipe90.sackbot.handler.dto.SettingsDTO
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono

@Component
class SettingsHandler(tts: TTS, voiceLines: VoiceLines, config: BotConfig) {

    private val settings = SettingsDTO(
            SettingsDTO.TTSSettings(
                    tts.isEnabled(),
                    tts.getAvailableVoices(),
                    tts.getMaxTextLength(),
                    tts.isRandomEnabled()
            ),
            SettingsDTO.VoiceSettings(
                    voiceLines.isEnabled(),
                    voiceLines.getVoiceLines()
            ),
            SettingsDTO.UploadSettings(
                    config.upload.sizeLimit,
                    config.upload.overrideExisting
            )
    )

    fun getSettings(request: ServerRequest, principal: DiscordUser): Mono<ServerResponse> {
        return ServerResponse.ok().bodyValue(settings)
    }
}