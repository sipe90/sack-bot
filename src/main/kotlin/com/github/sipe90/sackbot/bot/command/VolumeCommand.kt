package com.github.sipe90.sackbot.bot.command

import com.github.sipe90.sackbot.config.BotConfig
import com.github.sipe90.sackbot.service.AudioPlayerService
import com.github.sipe90.sackbot.util.getGuild
import net.dv8tion.jda.api.events.Event
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.kotlin.core.publisher.toMono

@Component
class VolumeCommand(private val config: BotConfig, private val playerService: AudioPlayerService) :
    BotCommand() {

    override val commandPrefix = "volume"

    override fun process(initiator: Event, vararg command: String): Flux<String> = Flux.defer {
        val guild = getGuild(initiator)
            ?: return@defer "Could not find guild or voice channel to perform the action".toMono()

        if (command.size == 1) {
            val volume = playerService.getDefaultVolume(guild.id)
            return@defer "Current volume is set to `$volume%`".toMono()
        }

        if (command.size != 2) return@defer helpText().toMono()

        val volumeStr = command[1]
        try {
            val volume = volumeStr.toInt()
                .coerceAtLeast(1)
                .coerceAtMost(100)

            playerService.setDefaultVolume(guild.id, volume)
            "Setting volume to `$volume%`".toMono()
        } catch (e: NumberFormatException) {
            "Failed to set volume. Could not parse amount".toMono()
        }
    }

    private fun helpText() = "Invalid volume command. " +
        "Say `${config.chat.commandPrefix}volume [volume]` to set the current volume. " +
        "Accepted values are 1-100. You can get the current volume with `${config.chat.commandPrefix}volume"
}