package com.github.sipe90.sackbot.service

import club.minnced.jda.reactor.asMono
import club.minnced.jda.reactor.createManager
import club.minnced.jda.reactor.on
import com.github.sipe90.sackbot.config.BotConfig
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.OnlineStatus
import net.dv8tion.jda.api.entities.ChannelType
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.PrivateChannel
import net.dv8tion.jda.api.events.ReadyEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import java.util.concurrent.Executor

@Service
final class BotServiceImpl(
    private val config: BotConfig,
    private val taskExecutor: Executor,
    private val fileService: AudioFileService,
    private val playerService: AudioPlayerService
) : BotService {

    private val cmdSplitRegex = Regex("\\s+")
    private val eventManager = createManager { scheduler = Schedulers.fromExecutor(taskExecutor); isDispose = false }
    private val jda = JDABuilder(config.token).setEventManager(eventManager).build()

    init {
        eventManager.on<ReadyEvent>()
            .next()
            .map { it.jda }
            .doOnSuccess { it.presence.setStatus(OnlineStatus.DO_NOT_DISTURB) }
            .subscribe()

        eventManager.on<MessageReceivedEvent>()
            .subscribe(this::onMessageReceived)
    }

    private fun onMessageReceived(event: MessageReceivedEvent) {
        if (event.author.isBot) return
        if (!config.chat.allowDm && event.isFromType(ChannelType.PRIVATE)) return

        event.author.openPrivateChannel().asMono().map {
            val msg = event.message.contentRaw

            if (!msg.startsWith(config.chat.commandPrefix)) {
                return@map helpCommand(event, it)
            }

            val cmdStr = msg.substring(1)
            val cmd = cmdSplitRegex.split(cmdStr)

            return@map when (cmd[0]) {
                "help" -> helpCommand(event, it)
                "info" -> infoCommand(cmd, event, it)
                "list" -> listCommand(cmd, event, it)
                "volume" -> volumeCommand(cmd, event, it)
                else -> playFileCommand(cmd, event, it)
            }
        }.subscribe()
    }

    private fun helpCommand(event: MessageReceivedEvent, channel: PrivateChannel): Mono<Message> {
        return channel.sendMessage("Not implemented").asMono()
    }

    private fun volumeCommand(
        cmd: List<String>,
        event: MessageReceivedEvent,
        channel: PrivateChannel
    ): Mono<Message> {
        return channel.sendMessage("Not implemented").asMono()
    }

    private fun playFileCommand(
        cmd: List<String>,
        event: MessageReceivedEvent,
        channel: PrivateChannel
    ): Mono<Message> {
        if (cmd.size != 2) {
            return channel.sendMessage("Invalid play command. Correct format is `$config.chat.commandPrefix <soundName>`")
                .asMono()
        }

        val audioFileName = cmd[1]

        return fileService.getAudioFileByName(audioFileName).flatMap {

            if (it == null) return@flatMap channel.sendMessage("Could not find a sound file with given name").asMono()

            val voiceChannel = when {
                event.isFromType(ChannelType.TEXT) -> event.member?.voiceState?.channel
                event.isFromType(ChannelType.PRIVATE) ->
                    event.author.mutualGuilds
                        .flatMap { guild -> guild.voiceChannels }
                        .find { vc -> vc.members.map { m -> m.id }.contains(event.author.id) }
                else -> null
            }
                ?: return@flatMap channel.sendMessage("Could not find a voice channel to play in").asMono()

            playerService.playInChannel(it.toString(), voiceChannel)
            channel.sendMessage("Playing sound file `$audioFileName`").asMono()
        }
    }

    private fun listCommand(
        cmd: List<String>,
        event: MessageReceivedEvent,
        channel: PrivateChannel
    ): Mono<Message> {
        return Mono.just("```")
            .concatWith(fileService.getAudioFiles().map { "\n$it" })
            .concatWith(Mono.just("\n```"))
            .reduce(StringBuilder(), { sb, str -> sb.append(str) })
            .flatMap { channel.sendMessage(it).asMono() }
    }

    private fun infoCommand(
        cmd: List<String>,
        event: MessageReceivedEvent,
        channel: PrivateChannel
    ): Mono<Message> {
        return channel.sendMessage("Not implemented").asMono()
    }
}

