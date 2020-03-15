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
import org.springframework.core.io.ByteArrayResource
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.io.ByteArrayOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

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

    @GetMapping("/{guildId}/sounds")
    @JsonView(API::class)
    fun getSoundsList(@PathVariable guildId: String, @AuthenticationPrincipal principal: DiscordUser): Flux<AudioFile> {
        return audioFileService.getAudioFiles(guildId, principal.getId())
    }

    @GetMapping("/{guildId}/sounds/export")
    fun exportSounds(
        @PathVariable guildId: String,
        @AuthenticationPrincipal principal: DiscordUser
    ): Mono<ResponseEntity<ByteArrayResource>> {
        return audioFileService.getAudioFiles(guildId, principal.getId())
            .collectList()
            .map {
                ByteArrayOutputStream().use { baos ->
                    val zip = ZipOutputStream(baos)
                    it.forEach {
                        val entry = ZipEntry(entryName(it.name, it.extension))
                        zip.putNextEntry(entry)
                        zip.write(it.data)
                        zip.closeEntry()
                    }
                    zip.finish()
                    baos
                }
            }.map {
                ResponseEntity.ok()
                    .contentType(MediaType.valueOf("application/zip"))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"export.zip\"")
                    .body(ByteArrayResource(it.toByteArray()))
            }
    }

    private fun entryName(fileName: String, extension: String?): String {
        if (extension == null) return fileName
        return "${fileName}.${extension}"
    }
}