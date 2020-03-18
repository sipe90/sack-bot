package com.github.sipe90.sackbot.controller

import com.fasterxml.jackson.annotation.JsonView
import com.github.sipe90.sackbot.auth.DiscordUser
import com.github.sipe90.sackbot.persistence.dto.API
import com.github.sipe90.sackbot.persistence.dto.AudioFile
import com.github.sipe90.sackbot.service.AudioFileService
import com.github.sipe90.sackbot.service.AudioPlayerService
import org.springframework.core.io.ByteArrayResource
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@RestController
@RequestMapping("{guildId}/sounds")
class AudioController(
    private val audioPlayerService: AudioPlayerService,
    private val audioFileService: AudioFileService
) {

    @PostMapping("/play")
    fun playSound(
        @PathVariable guildId: String,
        @RequestParam name: String,
        @AuthenticationPrincipal principal: DiscordUser
    ): Mono<Void> {
        return audioPlayerService.playAudioForUser(principal.getId(), name).then()
    }

    @GetMapping
    @JsonView(API::class)
    fun getSoundsList(@PathVariable guildId: String, @AuthenticationPrincipal principal: DiscordUser): Flux<AudioFile> {
        return audioFileService.getAudioFiles(guildId, principal.getId())
    }

    @GetMapping("/export")
    fun exportSounds(
        @PathVariable guildId: String,
        @AuthenticationPrincipal principal: DiscordUser
    ): Mono<ResponseEntity<ByteArrayResource>> {
        return audioFileService.zipFiles(guildId, principal.getId()).map {
            ResponseEntity.ok()
                .contentType(MediaType.valueOf("application/zip"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"export.zip\"")
                .body(ByteArrayResource(it))
        }
    }
}