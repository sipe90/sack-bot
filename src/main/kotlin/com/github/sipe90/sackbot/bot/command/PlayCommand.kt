package com.github.sipe90.sackbot.bot.command

import com.github.sipe90.sackbot.SackException
import com.github.sipe90.sackbot.service.AudioFileService
import com.github.sipe90.sackbot.service.AudioPlayerService
import com.github.sipe90.sackbot.util.getVoiceChannel
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.kotlin.core.publisher.toMono

@Component
class PlayCommand(private val fileService: AudioFileService, private val playerService: AudioPlayerService) :
    BotCommand() {

    final override val commandName = "play"

    final override val commandData = Commands.slash("play", "Play a sound")
        .addOption(OptionType.STRING, "sound", "Sound name", true)

    override fun process(initiator: SlashCommandInteractionEvent): Flux<String> = Flux.defer {
        val voiceChannel = getVoiceChannel(initiator.user)
            ?: return@defer "Could not find voice channel to perform the action".toMono()

        val soundOpt = initiator.getOption("sound") ?: throw SackException("Sound option missing from play command")
        val audioFileName = soundOpt.asString

        fileService.findAudioFile(voiceChannel.guild.id, audioFileName)
            .flatMap {
                playerService.playAudioInChannel(it.name, voiceChannel, null)
                    .then("Playing sound file `$audioFileName` in voice channel `#${voiceChannel.name}`".toMono())
            }
            .switchIfEmpty("Could not find any sounds with given name".toMono())
    }
}