package com.github.sipe90.sackbot.bot.event

import club.minnced.jda.reactor.asMono
import club.minnced.jda.reactor.toByteArray
import com.github.sipe90.sackbot.config.BotConfig
import com.github.sipe90.sackbot.exception.ValidationException
import com.github.sipe90.sackbot.service.AudioFileService
import com.github.sipe90.sackbot.service.JDAService
import com.github.sipe90.sackbot.util.getGuild
import com.github.sipe90.sackbot.util.stripExtension
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toFlux
import reactor.kotlin.core.publisher.toMono
import reactor.kotlin.core.util.function.component1
import reactor.kotlin.core.util.function.component2

@Component
final class MessageEventHandler(
    val config: BotConfig, private val jdaService: JDAService, private val fileService: AudioFileService
) : EventHandler<MessageReceivedEvent> {

    private final val logger = LoggerFactory.getLogger(javaClass)

    override fun handleEvent(event: MessageReceivedEvent) = processPrivateMessageEvent(event)
        .onErrorResume {
            logger.error("Error while processing private message event", it)
            "Encountered an error when processing command. Please try again later.".toMono()
        }.flatMap { message ->
            Mono.zip(event.author.openPrivateChannel().asMono(), message.toMono())
        }.flatMap { (channel, message) ->
            channel.sendMessage(message).asMono()
        }.then()

    private fun processPrivateMessageEvent(event: MessageReceivedEvent): Flux<String> {
        if (event.author.isBot) return Flux.empty()

        val guild = getGuild(event.author)
            ?: return Flux.just("Could not find guild or voice channel to perform the action.")

        val hasAdminAccess = jdaService.hasAdminAccess(event.author, guild)

        if (event.message.attachments.isNotEmpty()) {
            return if (hasAdminAccess) handleUploads(event, guild)
            else Flux.just("Admin access is required to upload sounds")
        }
        return if (hasAdminAccess) Flux.just("You can upload new sounds by sending them to me via a private message as attachments")
        else Flux.empty()
    }

    private fun handleUploads(event: MessageReceivedEvent, guild: Guild): Flux<String> =
        event.message.attachments.toFlux().flatMap flatMap@{ attachment ->
            val fileName = attachment.fileName
            val fileExtension = attachment.fileExtension
            val size = attachment.size

            val audioName = stripExtension(fileName)

            logger.info("Processing attachment {}", fileName)

            if (fileExtension != "wav" && fileExtension != "mp3" && fileExtension != "ogg") {
                return@flatMap "Could not upload file `${fileName}`: Unknown extension. Only `wav`, `.mp3` and `.ogg` are supported.".toMono()
            }
            if (size > config.upload.sizeLimit) {
                return@flatMap "Could not upload file `${fileName}`: File size cannot exceed `${config.upload.sizeLimit / 1000}kB`.".toMono()
            }

            attachment.toByteArray().flatMap { data ->
                fileService.findAudioFile(guild.id, audioName).flatMap exists@{ audioFile ->
                    if (!config.upload.overrideExisting) return@exists "Audio `${audioName}` already exists.".toMono()

                    audioFile.extension = fileExtension
                    audioFile.data = data

                    return@exists fileService.updateAudioFile(
                        guild.id, audioName, audioFile, event.author.id
                    )
                }.map { "Updated audio file `${audioName}`." }
                    .switchIfEmpty(
                        fileService.saveAudioFile(
                            guild.id,
                            audioName,
                            fileExtension,
                            HashSet(),
                            data,
                            event.author.id
                        ).map { "Saved audio file `${audioName}`." }
                    )
                    .onErrorResume(ValidationException::class.java) { "Failed to upload file: ${it.message}".toMono() }
            }
        }
}