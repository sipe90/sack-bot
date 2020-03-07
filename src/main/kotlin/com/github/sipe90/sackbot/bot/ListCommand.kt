package com.github.sipe90.sackbot.bot

import club.minnced.jda.reactor.toMono
import com.github.sipe90.sackbot.service.AudioFileService
import com.github.sipe90.sackbot.util.getApplicableGuild
import net.dv8tion.jda.api.events.Event
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class ListCommand(private val fileService: AudioFileService) : BotCommand {

    override val commandPrefix = "list"

    override fun process(initiator: Event, vararg command: String): Mono<String> = Mono.defer {
        val guild = getApplicableGuild(initiator)
            ?: return@defer "Could not find guild or voice channel to perform the action".toMono()
        fileService.getAudioFiles(guild.id).map { "\n${it.name}" }
            .reduce(StringBuilder(), { sb, str -> sb.append(str) })
            .map { String.format("List of available sounds to play:\n```%s\n```", it) }
    }
}