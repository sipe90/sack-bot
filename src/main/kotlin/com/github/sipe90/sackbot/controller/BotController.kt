package com.github.sipe90.sackbot.controller

import com.fasterxml.jackson.annotation.JsonView
import com.github.sipe90.sackbot.auth.DiscordUser
import com.github.sipe90.sackbot.persistence.MemberRepository
import com.github.sipe90.sackbot.persistence.dto.API
import com.github.sipe90.sackbot.persistence.dto.AudioFile
import com.github.sipe90.sackbot.persistence.dto.Member
import com.github.sipe90.sackbot.service.AudioFileService
import com.github.sipe90.sackbot.service.AudioPlayerService
import com.github.sipe90.sackbot.service.JDAService
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux

@RestController
class BotController(
    private val audioFileService: AudioFileService,
    private val audioPlayerService: AudioPlayerService,
    private val memberRepository: MemberRepository,
    private val jdaService: JDAService
) {

    @GetMapping("/me")
    fun userInfo(@AuthenticationPrincipal principal: DiscordUser): Flux<Member> {
        return memberRepository.getUserMembers(principal.getId())
    }

    @GetMapping("/sounds")
    @JsonView(API::class)
    fun getSoundsList(@RequestParam guildId: String, @AuthenticationPrincipal principal: DiscordUser): Flux<AudioFile> {
        return audioFileService.getAudioFiles(guildId, principal.getId())
    }
}