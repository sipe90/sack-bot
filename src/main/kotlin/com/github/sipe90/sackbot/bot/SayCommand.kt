package com.github.sipe90.sackbot.bot

import club.minnced.jda.reactor.toMono
import com.github.sipe90.sackbot.config.BotConfig
import com.github.sipe90.sackbot.service.AudioPlayerService
import net.dv8tion.jda.api.events.Event
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class SayCommand(private val config: BotConfig, private val playerService: AudioPlayerService) : AbstractBotCommand() {

    override val commandPrefix = "say"

    override fun process(initiator: Event, vararg command: String): Mono<String> {
        val voiceChannel = getVoiceChannel(initiator)
            ?: return "Could not find guild or voice channel to perform the action".toMono()
        if (command.size < 2) {
            return "Invalid say command. Correct format is `${config.chat.commandPrefix}say <text>`".toMono()
        }

        val text = command.slice(1 until command.size).joinToString(" ")

        return playerService.playTtsInChannel(text, voiceChannel)
            .flatMap { "Playing text to speech in voice channel `#${voiceChannel.name}`".toMono() }
    }
}