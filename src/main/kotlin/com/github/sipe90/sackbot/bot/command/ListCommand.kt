package com.github.sipe90.sackbot.bot.command

import com.github.sipe90.sackbot.service.AudioFileService
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class ListCommand(private val fileService: AudioFileService) : BotCommand() {

    final override val commandName = "list"

    final override val commandData = Commands.slash("list", "List all sounds")
        .addOption(OptionType.STRING, "tag", "Filter by tag", false)

    override fun process(
        initiator: SlashCommandInteractionEvent,
        guild: Guild?,
        voiceChannel: VoiceChannel?,
    ): Mono<Unit> {
        if (guild == null) {
            return sendMessage(initiator, "Cannot determine guild. You may need to join a voice channel first.")
        }

        val tag = initiator.getOption("tag")?.asString

        // FIXME: Pagination
        return fileService.getAudioFiles(guild.id)
            .filter { tag == null || it.tags.contains(tag) }
            .map { "\n${it.name}" }
            .reduce(StringBuilder()) { sb, str -> sb.append(str) }
            .map { String.format("List of available sounds to play:\n```%s\n```", it) }
            .flatMap { sendMessage(initiator, it) }
    }
}
