package com.github.sipe90.sackbot.bot.command

import com.github.sipe90.sackbot.service.AudioFileService
import com.github.sipe90.sackbot.service.AudioPlayerService
import com.github.sipe90.sackbot.util.getVoiceChannel
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.kotlin.core.publisher.toMono

@Component
class RandomCommand(private val fileService: AudioFileService, private val playerService: AudioPlayerService) :
    BotCommand() {

    final override val commandName = "random"

    final override val commandData = CommandData("random", "Play a random sound")
        .addOption(OptionType.STRING, "tag", "Filter by tag", false)

    override fun process(initiator: SlashCommandEvent): Flux<String> = Flux.defer {
        val voiceChannel = getVoiceChannel(initiator)
            ?: return@defer "Could not find voice channel to perform the action".toMono()

        fileService.randomAudioFile(voiceChannel.guild.id, initiator.user.id)
            .flatMap { audioFile ->
                playerService.playAudioInChannel(audioFile.name, voiceChannel, null).then(audioFile.toMono())
            }
            .map { "Playing random sound file `${it.name}` in voice channel `#${voiceChannel.name}`" }
    }
}