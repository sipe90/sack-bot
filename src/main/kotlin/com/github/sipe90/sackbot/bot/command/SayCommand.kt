package com.github.sipe90.sackbot.bot.command

import com.github.sipe90.sackbot.component.VoiceLines
import com.github.sipe90.sackbot.service.AudioPlayerService
import com.github.sipe90.sackbot.util.getVoiceChannel
import net.dv8tion.jda.api.events.Event
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.toFlux
import reactor.core.publisher.toMono

@Component
class SayCommand(
    private val voiceLines: VoiceLines,
    private val playerService: AudioPlayerService
) :
    BotCommand {

    override val commandPrefix = "say"

    override fun process(initiator: Event, vararg command: String): Flux<String> = Flux.defer {
        val voiceChannel = getVoiceChannel(initiator)
            ?: return@defer "Could not find guild or voice channel to perform the action".toMono()
        val voices = voiceLines.getVoices()
        if (voices.isEmpty()) {
            return@defer "There are no voices available!".toMono()
        }
        if (command.size < 2) {
            return@defer "Available voices: `${voices.joinToString("`, `")}`".toMono()
        }
        val voice = command[1]
        if (!voiceLines.voiceIsAvailable(voice)) {
            return@defer "Invalid voice given. Available voices are: `${voices.joinToString(", ")}`".toMono()
        }
        if (command.size < 3) {
            return@defer Flux.just("Available voice lines for voice `${voice}`:\n")
                .concatWith(voiceLines.getVoiceLines(voice).chunked(100) {
                    "```${it.joinToString("\n")}```"
                }.toFlux())
        }

        val lines = command.slice(2 until command.size)

        playerService.playVoiceLinesInChannel(voice, lines, voiceChannel)
            .map { "Playing ${lines.size} voice lines with voice `${voice}` on channel #${voiceChannel.name}" }
    }
}