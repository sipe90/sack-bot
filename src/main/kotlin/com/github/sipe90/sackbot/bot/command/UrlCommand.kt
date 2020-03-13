package com.github.sipe90.sackbot.bot.command

import club.minnced.jda.reactor.toMono
import com.github.sipe90.sackbot.config.BotConfig
import com.github.sipe90.sackbot.service.AudioPlayerService
import com.github.sipe90.sackbot.util.getApplicableGuild
import com.github.sipe90.sackbot.util.getVoiceChannel
import net.dv8tion.jda.api.events.Event
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class UrlCommand(
    private val config: BotConfig,
    private val playerService: AudioPlayerService
) : BotCommand {

    override val commandPrefix = "url"

    override fun canProcess(vararg command: String) = true

    override fun process(initiator: Event, vararg command: String): Mono<String> = Mono.defer {
        val guild = getApplicableGuild(initiator)
        val voiceChannel = getVoiceChannel(initiator)

        if (guild == null || voiceChannel == null) return@defer "Could not find guild or voice channel to perform the action".toMono()

        if (command.size != 2) {
            return@defer "Invalid url command. Correct format is `${config.chat.commandPrefix}url <url>`".toMono()
        }

        val url = command[1]

        playerService.playUrlInChannel(url, voiceChannel)
            .map {
                return@map if (it) "Playing url `$url` in voice channel `#${voiceChannel.name}`"
                else "Could not find a sound source with given url"
            }
    }
}