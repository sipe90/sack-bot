package com.github.sipe90.sackbot.bot.command

import com.github.sipe90.sackbot.service.AudioFileService
import com.github.sipe90.sackbot.util.getGuild
import com.github.sipe90.sackbot.util.getUser
import net.dv8tion.jda.api.events.Event
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.kotlin.core.publisher.toMono

@Component
class ListCommand(private val fileService: AudioFileService) :
    BotCommand {

    override val commandPrefix = "list"

    override fun process(initiator: Event, vararg command: String): Flux<String> = Flux.defer {
        val guild = getGuild(initiator)
            ?: return@defer "Could not find guild or voice channel to perform the action".toMono()
        val user = getUser(initiator) ?: return@defer "Could not find user".toMono()
        fileService.getAudioFiles(guild.id, user.id).map { "\n${it.name}" }
            .reduce(StringBuilder(), { sb, str -> sb.append(str) })
            .map { String.format("List of available sounds to play:\n```%s\n```", it) }
    }
}