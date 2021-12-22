package com.github.sipe90.sackbot.bot.command

import com.github.sipe90.sackbot.service.AudioFileService
import com.github.sipe90.sackbot.util.getGuild
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.kotlin.core.publisher.toMono

@Component
class ListCommand(private val fileService: AudioFileService) : BotCommand() {

    final override val commandName = "list"

    final override val commandData = CommandData("list", "List all sounds")
        .addOption(OptionType.STRING, "tag", "Filter by tag", false)

    override fun process(initiator: SlashCommandEvent): Flux<String> = Flux.defer {
        val guild = getGuild(initiator)
            ?: return@defer "Could not find voice channel to perform the action".toMono()

        // FIXME: Pagination
        fileService.getAudioFiles(guild.id).map { "\n${it.name}" }
            .reduce(StringBuilder()) { sb, str -> sb.append(str) }
            .map { String.format("List of available sounds to play:\n```%s\n```", it) }
    }
}