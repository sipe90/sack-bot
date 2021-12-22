package com.github.sipe90.sackbot.bot.command

import com.github.sipe90.sackbot.service.AudioPlayerService
import com.github.sipe90.sackbot.util.getGuild
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.kotlin.core.publisher.toMono

@Component
class VolumeCommand(private val playerService: AudioPlayerService) : BotCommand() {

    final override val commandName = "volume"

    final override val commandData = CommandData(commandName, "Check or set bot volume")
        .addOption(OptionType.INTEGER, "volume", "New volume (0-100)", false)

    override fun process(initiator: SlashCommandEvent): Flux<String> = Flux.defer {
        val guild = getGuild(initiator)
            ?: return@defer "Could not find guild or voice channel to perform the action".toMono()

        val volumeOpt = initiator.getOption("volume")

        if (volumeOpt == null) {
            val volume = playerService.getDefaultVolume(guild.id)
            return@defer "Current volume is set to `$volume%`".toMono()
        }

        val volume = volumeOpt.asLong.coerceIn(1, 100)

        playerService.setDefaultVolume(guild.id, volume.toInt())
        "Setting volume to `$volume%`".toMono()
    }
}