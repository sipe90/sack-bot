package com.github.sipe90.sackbot.bot

import com.github.sipe90.sackbot.service.AudioFileService
import net.dv8tion.jda.api.events.Event
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class ListCommand(private val fileService: AudioFileService) : AbstractBotCommand() {

    override val commandPrefix = "list"

    override fun process(initiator: Event, vararg command: String): Mono<String> =
        fileService.getAudioFiles().map { "\n$it" }
            .reduce(StringBuilder(), { sb, str -> sb.append(str) })
            .map { String.format("List of available sounds to play:\n```%s\n```", it) }
}