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
        BotCommand() {

    override val commandPrefix = "tts"

    override fun process(initiator: Event, vararg command: String): Flux<String> = Flux.defer {
        val voiceChannel = getVoiceChannel(initiator)
                ?: return@defer "Could not find guild or voice channel to perform the action".toMono()

        if (command.size == 1) {
            return@defer "Invalid say command. Correct format is `${config.chat.commandPrefix}tts <voice> <text>`".toMono()
        }

        val voice = command[1]

        if (command.size < 3) {

            if (!playerService.isRandomTtsEnabled()) {
                return@defer "Invalid say command. Correct format is `${config.chat.commandPrefix}tts <voice> <text>`".toMono()
            }

            return@defer "Playing random text to speech phrase in voice channel `#${voiceChannel.name}`".toMono()
                    .concatWith(
                            playerService.playRandomTtsInChannel(voice, voiceChannel).then(Mono.empty())
                    )
        }

        val text = command.slice(2 until command.size).joinToString(" ")

        val mono = "Playing text to speech in voice channel `#${voiceChannel.name}`".toMono().concatWith(
                playerService.playTtsInChannel(voice, text, voiceChannel).then(Mono.empty())
        )

        return@defer if (text.length > config.tts.maxLength)
            "Given text is more than ${config.tts.maxLength} characters long, all characters exceeding the limit will be cut".toMono().concatWith(mono)
        else mono
    }
}