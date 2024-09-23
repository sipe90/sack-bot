package com.github.sipe90.sackbot.bot.event

import club.minnced.jda.reactor.asMono
import club.minnced.jda.reactor.toByteArray
import com.github.sipe90.sackbot.config.BotConfig
import com.github.sipe90.sackbot.exception.ValidationException
import com.github.sipe90.sackbot.service.AudioFileService
import com.github.sipe90.sackbot.service.JDAService
import com.github.sipe90.sackbot.util.getGuild
import com.github.sipe90.sackbot.util.stripExtension
import mu.KotlinLogging
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toFlux

@Component
final class MessageEventHandler(
    val config: BotConfig,
    private val fileService: AudioFileService,
    private val jdaService: JDAService,
) : EventHandler<MessageReceivedEvent> {
    private val logger = KotlinLogging.logger {}

    override fun handleEvent(event: MessageReceivedEvent): Mono<Unit> {
        return processPrivateMessageEvent(event)
            .onErrorResume {
                logger.error(it) { "Error while processing private message event" }
                sendMessage(event, "Encountered an error when processing command. Please try again later.")
            }
    }

    private fun processPrivateMessageEvent(event: MessageReceivedEvent): Mono<Unit> {
        if (event.author.isBot) return Mono.empty()

        val guild =
            getGuild(event.author)
                ?: return sendMessage(event, "Could not find guild or voice channel to perform the action.")
        val hasAdminAccess = jdaService.hasAdminAccess(event.author, guild)

        if (event.message.attachments.isNotEmpty()) {
            return if (hasAdminAccess) {
                handleUploads(event, guild)
            } else {
                sendMessage(event, "Admin access is required to upload sounds")
            }
        }
        return if (hasAdminAccess) {
            sendMessage(event, "You can upload new sounds by sending them to me via a private message as attachments")
        } else {
            Mono.empty()
        }
    }

    private fun handleUploads(
        event: MessageReceivedEvent,
        guild: Guild,
    ): Mono<Unit> {
        return event.message.attachments.toFlux().flatMap { attachment ->
            val fileName = attachment.fileName
            val fileExtension = attachment.fileExtension
            val size = attachment.size

            val audioName = stripExtension(fileName)

            logger.info { "Processing attachment $fileName" }

            if (fileExtension != "wav" && fileExtension != "mp3" && fileExtension != "ogg") {
                Mono.just("Could not upload file `$fileName`: Unknown extension. Only `wav`, `.mp3` and `.ogg` are supported.")
            } else if (size > config.upload.sizeLimit) {
                Mono.just("Could not upload file `$fileName`: File size cannot exceed `${config.upload.sizeLimit / 1000}kB`.")
            } else {
                attachment.toByteArray().flatMap { data ->
                    fileService.findAudioFile(guild.id, audioName).flatMap { audioFile ->
                        if (!config.upload.overrideExisting) {
                            Mono.just("Audio `$audioName` already exists.")
                        } else {
                            audioFile.extension = fileExtension
                            audioFile.data = data

                            fileService.updateAudioFile(
                                guild.id,
                                audioName,
                                audioFile,
                                event.author.id,
                            ).then(Mono.just("Updated audio file `$audioName`."))
                        }
                    }
                        .switchIfEmpty(
                            fileService.saveAudioFile(
                                guild.id,
                                audioName,
                                fileExtension,
                                data,
                                event.author.id,
                            ).then(Mono.just("Saved audio file `$audioName`.")),
                        )
                        .onErrorResume(ValidationException::class.java) { Mono.just("Failed to upload file: ${it.message}") }
                }
            }
        }
            .flatMap { sendMessage(event, it) }
            .then(Mono.empty())
    }

    private fun sendMessage(
        event: MessageReceivedEvent,
        message: String,
    ): Mono<Unit> =
        event.author.openPrivateChannel()
            .flatMap { channel -> channel.sendMessage(message) }
            .asMono()
            .then(Mono.empty())
}
