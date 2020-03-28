package com.github.sipe90.sackbot.bot.event

import club.minnced.jda.reactor.asMono
import club.minnced.jda.reactor.toByteArray
import com.github.sipe90.sackbot.SackException
import com.github.sipe90.sackbot.bot.command.BotCommand
import com.github.sipe90.sackbot.config.BotConfig
import com.github.sipe90.sackbot.service.AudioFileService
import com.github.sipe90.sackbot.util.getGuild
import com.github.sipe90.sackbot.util.stripExtension
import net.dv8tion.jda.api.events.Event
import net.dv8tion.jda.api.events.GenericEvent
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toFlux
import reactor.kotlin.core.publisher.toMono
import reactor.util.function.Tuples

@Component
final class MessageEventHandler(
    val config: BotConfig,
    private val fileService: AudioFileService,
    commands: List<BotCommand>
) : EventHandler<GenericEvent> {

    private final val logger = LoggerFactory.getLogger(javaClass)

    private val cmdSplitRegex = Regex("\\s+")

    private val commandsMap = commands.associateBy(BotCommand::commandPrefix)
    private val helpCommand = commandsMap["help"] ?: throw SackException("Help command not found")
    private val playCommand = commandsMap[""] ?: throw SackException("Play command not found")

    override fun handleEvent(event: GenericEvent): Mono<Void> {
        if (event is GuildMessageReceivedEvent) {
            return processGuildMessageEvent(event)
                .flatMap { event.channel.sendMessage(it).asMono() }
                .then()
        }
        if (event is PrivateMessageReceivedEvent) {
            return processPrivateMessageEvent(event)
                .flatMap { f -> Mono.defer { event.author.openPrivateChannel().asMono() }.map { m -> Tuples.of(m, f) } }
                .flatMap { it.t1.sendMessage(it.t2).asMono() }
                .then()
        }
        throw SackException("Invalid event: ${event.javaClass.name}")
    }

    private fun processGuildMessageEvent(event: GuildMessageReceivedEvent): Flux<String> {
        if (event.author.isBot) return Flux.empty()
        if (!event.message.contentRaw.startsWith(config.chat.commandPrefix)) return Flux.empty()
        return processCommand(event, event.message.contentRaw)
    }

    private fun processPrivateMessageEvent(event: PrivateMessageReceivedEvent): Flux<String> {
        if (event.author.isBot) return Flux.empty()
        if (event.message.attachments.isNotEmpty()) return handleUploads(event)
        if (!event.message.contentRaw.startsWith(config.chat.commandPrefix)) {
            return helpCommand.process(event)
        }
        return processCommand(event, event.message.contentRaw)
    }

    private fun processCommand(event: Event, message: String): Flux<String> {
        val cmd = cmdSplitRegex.split(message.substring(1)).toTypedArray()
        val botCommand = commandsMap[cmd[0]] ?: playCommand
        if (botCommand.canProcess(*cmd)) {
            return botCommand.process(event, *cmd)
        }
        return Flux.empty()
    }

    private fun handleUploads(event: PrivateMessageReceivedEvent): Flux<String> =
        event.message.attachments.toFlux()
            .flatMap flatMap@{ attachment ->
                val guild = getGuild(event)
                    ?: return@flatMap "Could not find guild or voice channel to perform the action".toMono()

                val fileName = attachment.fileName
                val fileExtension = attachment.fileExtension
                val size = attachment.size

                val audioName = stripExtension(fileName)

                logger.info("Processing attachment {}", fileName)

                if (fileExtension != "wav" && fileExtension != "mp3") {
                    return@flatMap "Could not upload file `${fileName}`: Unknown extension. Only mp3 and wav are supported.".toMono()
                }
                if (size > config.upload.sizeLimit) {
                    return@flatMap "Could not upload file `${fileName}`: File size cannot exceed `${config.upload.sizeLimit / 1000}kB`".toMono()
                }

                return@flatMap attachment.toByteArray().flatMap { data ->
                    fileService.findAudioFile(guild.id, audioName)
                        .flatMap exists@{ audioFile ->
                            if (!config.upload.overrideExisting) return@exists "Audio `${audioName}` already exists".toMono()

                            audioFile.extension = fileExtension
                            audioFile.data = data

                            return@exists fileService.updateAudioFile(
                                guild.id,
                                audioName,
                                audioFile,
                                event.author.id
                            )
                        }
                        .map { "Updated audio file `${audioName}`" }
                        .switchIfEmpty(
                            fileService.saveAudioFile(
                                guild.id,
                                audioName,
                                fileExtension,
                                data,
                                event.author.id
                            ).map { "Saved audio file `${audioName}`" }
                        )
                }
            }
}