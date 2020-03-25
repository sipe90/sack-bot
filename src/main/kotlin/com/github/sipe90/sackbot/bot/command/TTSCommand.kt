package com.github.sipe90.sackbot.bot.command

import com.github.sipe90.sackbot.config.BotConfig
import com.github.sipe90.sackbot.service.AudioPlayerService
import com.github.sipe90.sackbot.util.getVoiceChannel
import net.dv8tion.jda.api.events.Event
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

@Component
class TTSCommand(private val config: BotConfig, private val playerService: AudioPlayerService) :
    BotCommand {

    override val commandPrefix = "tts"

    override fun process(initiator: Event, vararg command: String): Flux<String> = Flux.defer {
        val voiceChannel = getVoiceChannel(initiator)
            ?: return@defer "Could not find guild or voice channel to perform the action".toMono()
        if (command.size < 2) {

            return@defer "Playing random text to speech phrase in voice channel `#${voiceChannel.name}`".toMono()
                .concatWith(
                    playerService.playRandomTtsInChannel(voiceChannel).flatMap {
                        if (it) Mono.empty() else "Invalid say command. Correct format is `${config.chat.commandPrefix}say <text>`".toMono()
                    }
                )
        }

        val text = command.slice(1 until command.size).joinToString(" ")

        "Playing text to speech in voice channel `#${voiceChannel.name}`".toMono().concatWith(
            playerService.playTtsInChannel(text, voiceChannel).then(Mono.empty())
        )
    }
}