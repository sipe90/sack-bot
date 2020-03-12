package com.github.sipe90.sackbot.controller

import com.github.sipe90.sackbot.persistence.dto.AudioFile
import com.github.sipe90.sackbot.service.AudioFileService
import com.github.sipe90.sackbot.service.AudioPlayerService
import com.github.sipe90.sackbot.service.BotService
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux

@RestController
class BotCommandController(
    private val audioFileService: AudioFileService,
    private val audioPlayerService: AudioPlayerService,
    private val botService: BotService
) {

    /*
    @GetMapping("/me")
    fun userInfo(@AuthenticationPrincipal principal: OAuth2User): Mono<OAuth2User> {
        return Mono.just(principal)
    }
     */

    @GetMapping("/sounds")
    fun getSoundsList(@AuthenticationPrincipal principal: OAuth2User): Flux<AudioFile> {
        return audioFileService.getAudioFiles("")
    }

    @GetMapping("/guilds")
    fun getGuilds(@AuthenticationPrincipal principal: OAuth2User): Flux<AudioFile> {
        return audioFileService.getAudioFiles("")
    }
}