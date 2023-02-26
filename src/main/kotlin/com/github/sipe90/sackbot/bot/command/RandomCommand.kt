package com.github.sipe90.sackbot.bot.command

import com.github.sipe90.sackbot.service.AudioFileService
import com.github.sipe90.sackbot.service.AudioPlayerService
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class RandomCommand(private val fileService: AudioFileService, private val playerService: AudioPlayerService) :
    BotCommand() {

    final override val commandName = "random"

    final override val commandData = Commands.slash("random", "Play a random sound")
        .addOption(OptionType.STRING, "tag", "Filter by tag", false)

    override fun process(
        initiator: SlashCommandInteractionEvent,
        guild: Guild?,
        voiceChannel: VoiceChannel?,
    ): Mono<Unit> {
        if (voiceChannel == null) {
            return sendMessage(initiator, "Could not find voice channel to perform the action")
        }

        val tags = initiator.getOption("tag")?.asString?.let { setOf(it) } ?: emptySet()

        return fileService.randomAudioFile(voiceChannel.guild.id, initiator.user.id, tags)
            .flatMap { audioFile ->
                playerService.playAudioInChannel(audioFile.name, voiceChannel)
                    .then(Mono.just("Playing random sound file `${audioFile.name}` in voice channel `#${voiceChannel.name}`"))
            }
            .defaultIfEmpty("Couldn't find any sound with given tag")
            .flatMap { sendMessage(initiator, it) }
    }
}
