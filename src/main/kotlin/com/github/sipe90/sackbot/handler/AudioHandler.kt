package com.github.sipe90.sackbot.handler

import com.github.sipe90.sackbot.auth.DiscordUser
import com.github.sipe90.sackbot.exception.BadRequestException
import com.github.sipe90.sackbot.exception.NotFoundException
import com.github.sipe90.sackbot.handler.dto.AudioFileUpdateDTO
import com.github.sipe90.sackbot.service.AudioFileService
import com.github.sipe90.sackbot.service.AudioPlayerService
import com.github.sipe90.sackbot.service.MemberService
import com.github.sipe90.sackbot.util.getExtension
import com.github.sipe90.sackbot.util.stripExtension
import com.github.sipe90.sackbot.util.withExtension
import org.springframework.core.io.buffer.DataBufferUtils
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.codec.multipart.FilePart
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.BodyExtractors
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.ServerResponse.noContent
import org.springframework.web.reactive.function.server.ServerResponse.notFound
import org.springframework.web.reactive.function.server.ServerResponse.ok
import org.springframework.web.reactive.function.server.body
import org.springframework.web.reactive.function.server.bodyToMono
import org.springframework.web.reactive.function.server.queryParamOrNull
import reactor.core.publisher.Mono
import reactor.kotlin.core.util.function.component1
import reactor.kotlin.core.util.function.component2

@Component
class AudioHandler(
    private val audioPlayerService: AudioPlayerService,
    private val audioFileService: AudioFileService,
    private val memberService: MemberService,
) {

    fun playSound(request: ServerRequest, principal: DiscordUser): Mono<ServerResponse> {
        val guildId = request.pathVariable("guildId")
        val name = request.pathVariable("name")
        val userId = principal.getId()

        return audioPlayerService.playAudioForUser(guildId, userId, name)
            .flatMap { noContent().build() }
    }

    fun playRandomSound(request: ServerRequest, principal: DiscordUser): Mono<ServerResponse> {
        val guildId = request.pathVariable("guildId")
        val userId = principal.getId()
        val tags = request.queryParams()["tags"]

        return audioFileService.randomAudioFile(guildId, userId, tags.orEmpty().toHashSet())
            .flatMap { audioPlayerService.playAudioForUser(guildId, userId, it.name) }
            .flatMap { noContent().build() }
    }

    fun playUrl(request: ServerRequest, principal: DiscordUser): Mono<ServerResponse> {
        val guildId = request.pathVariable("guildId")
        val userId = principal.getId()
        val url = request.queryParam("url").orElseThrow { BadRequestException("Query parameter url is required") }

        return audioPlayerService.playUrlForUser(guildId, userId, url)
            .flatMap { noContent().build() }
    }

    fun getSoundsList(request: ServerRequest, principal: DiscordUser): Mono<ServerResponse> {
        val guildId = request.pathVariable("guildId")

        return ok()
            .body(audioFileService.getAudioFiles(guildId))
    }

    fun updateSound(request: ServerRequest, principal: DiscordUser): Mono<ServerResponse> {
        val guildId = request.pathVariable("guildId")
        val name = request.pathVariable("name")
        val userId = principal.getId()

        return request.bodyToMono<AudioFileUpdateDTO>()
            .zipWith(
                audioFileService.findAudioFile(guildId, name)
                    .switchIfEmpty(Mono.error(NotFoundException("Audio file not found"))),
            )
            .flatMap { (dto, audioFile) ->
                audioFile.name = dto.name
                audioFile.tags = dto.tags
                audioFileService.updateAudioFile(guildId, name, audioFile, userId)
            }
            .flatMap { noContent().build() }
    }

    fun uploadSounds(request: ServerRequest, principal: DiscordUser): Mono<ServerResponse> {
        val guildId = request.pathVariable("guildId")
        val userId = principal.getId()

        return request.body(BodyExtractors.toParts())
            .filter { it is FilePart }
            .flatMap { part ->
                val filePart = part as FilePart
                val fileName = filePart.filename()
                val audioName = stripExtension(fileName)
                val fileExtension = getExtension(fileName)

                DataBufferUtils.join(filePart.content()).map { dataBuffer ->
                    val bytes = ByteArray(dataBuffer.readableByteCount())
                    dataBuffer.read(bytes)
                    DataBufferUtils.release(dataBuffer)
                    bytes
                }.flatMap { data ->
                    audioFileService.findAudioFile(guildId, audioName)
                        .flatMap { audioFile ->

                            audioFile.extension = fileExtension
                            audioFile.data = data

                            audioFileService.updateAudioFile(
                                guildId,
                                audioName,
                                audioFile,
                                userId,
                            )
                        }
                        .switchIfEmpty(
                            audioFileService.saveAudioFile(
                                guildId,
                                audioName,
                                fileExtension,
                                data,
                                userId,
                            ),
                        )
                }
            }.then(noContent().build())
    }

    fun setEntrySound(request: ServerRequest, principal: DiscordUser): Mono<ServerResponse> {
        val guildId = request.pathVariable("guildId")
        val userId = principal.getId()
        val name = request.queryParam("name").orElse(null)

        return memberService.setMemberEntrySound(guildId, userId, name).then(ok().build())
    }

    fun setExitSound(request: ServerRequest, principal: DiscordUser): Mono<ServerResponse> {
        val guildId = request.pathVariable("guildId")
        val userId = principal.getId()
        val name = request.queryParam("name").orElse(null)

        return memberService.setMemberExitSound(guildId, userId, name).then(ok().build())
    }

    fun deleteSound(request: ServerRequest, principal: DiscordUser): Mono<ServerResponse> {
        val guildId = request.pathVariable("guildId")
        val name = request.pathVariable("name")

        return audioFileService.deleteAudioFile(guildId, name)
            .flatMap { noContent().build() }
            .switchIfEmpty(notFound().build())
    }

    fun downloadSound(request: ServerRequest, principal: DiscordUser): Mono<ServerResponse> {
        val guildId = request.pathVariable("guildId")
        val name = request.pathVariable("name")

        return audioFileService.findAudioFile(guildId, name).flatMap {
            ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(
                    HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=\"${withExtension(it.name, it.extension)}\"",
                )
                .bodyValue(it.data)
        }.switchIfEmpty(notFound().build())
    }

    fun exportSounds(request: ServerRequest, principal: DiscordUser): Mono<ServerResponse> {
        val guildId = request.pathVariable("guildId")
        val userId = principal.getId()

        return ok()
            .contentType(MediaType.valueOf("application/zip"))
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"export.zip\"")
            .body(audioFileService.zipFiles(guildId, userId))
    }

    private fun getVolume(request: ServerRequest): Int? {
        val volumeStr = request.queryParamOrNull("vol") ?: return null
        try {
            return Integer.parseInt(volumeStr).coerceIn(0, 100)
        } catch (e: NumberFormatException) {
            throw BadRequestException("Invalid volume parameter")
        }
    }
}
