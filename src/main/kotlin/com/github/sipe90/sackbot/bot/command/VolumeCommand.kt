package com.github.sipe90.sackbot.bot.command

import com.github.sipe90.sackbot.service.AudioPlayerService
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class VolumeCommand(private val playerService: AudioPlayerService) : BotCommand() {

    final override val commandName = "volume"

    final override val commandData = Commands.slash(commandName, "Check or set bot volume")
        .addOptions(
            OptionData(OptionType.INTEGER, "volume", "New volume (0-100)", false)
                .setMinValue(0).setMaxValue(100),
        ).setDefaultPermissions(DefaultMemberPermissions.DISABLED)

    override fun process(
        initiator: SlashCommandInteractionEvent,
        guild: Guild?,
        voiceChannel: VoiceChannel?,
    ): Mono<Unit> {
        if (guild == null) {
            return sendMessage(initiator, "Cannot determine guild. You may need to join a voice channel first.")
        }

        val volumeOpt = initiator.getOption("volume")

        if (volumeOpt == null) {
            val volume = playerService.getVolume(guild.id)
            return sendMessage(initiator, "Current volume is set to `$volume%`")
        }

        val volume = volumeOpt.asLong.coerceIn(1, 100)

        return playerService.setVolume(guild.id, volume.toInt())
            .then(sendMessage(initiator, "Setting volume to `$volume%`"))
    }
}
