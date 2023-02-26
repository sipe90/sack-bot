package com.github.sipe90.sackbot.bot.command

import com.github.sipe90.sackbot.SackException
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
class PlayCommand(private val fileService: AudioFileService, private val playerService: AudioPlayerService) :
    BotCommand() {

    final override val commandName = "play"

    final override val commandData = Commands.slash("play", "Play a sound")
        .addOption(OptionType.STRING, "sound", "Sound name", true)

    override fun process(
        initiator: SlashCommandInteractionEvent,
        guild: Guild?,
        voiceChannel: VoiceChannel?,
    ): Mono<Unit> {
        if (voiceChannel == null) {
            return sendMessage(initiator, "Could not find voice channel to perform the action")
        }

        val soundOpt = initiator.getOption("sound") ?: throw SackException("Sound option missing from play command")
        val audioFileName = soundOpt.asString

        return fileService.findAudioFile(voiceChannel.guild.id, audioFileName)
            .flatMap {
                playerService.playAudioInChannel(it.name, voiceChannel)
                    .then(Mono.just("Playing sound file `$audioFileName` in voice channel `#${voiceChannel.name}`"))
            }
            .defaultIfEmpty("Could not find any sounds with given name")
            .flatMap { sendMessage(initiator, it) }
    }
}
