package com.github.sipe90.sackbot.controller

import com.github.sipe90.sackbot.persistence.dto.AudioFile
import com.github.sipe90.sackbot.service.AudioFileService
import com.github.sipe90.sackbot.service.AudioPlayerService
import com.github.sipe90.sackbot.service.BotService
import org.springframework.security.oauth2.core.OAuth2AccessToken
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux

@RestController
class BotCommandController(
    private val audioFileService: AudioFileService,
    private val audioPlayerService: AudioPlayerService,
    private val botService: BotService
) {

    @GetMapping("/sounds")
    fun getSoundsList(principal: OAuth2AccessToken): Flux<AudioFile> {
        return audioFileService.getAudioFiles("")
    }
}