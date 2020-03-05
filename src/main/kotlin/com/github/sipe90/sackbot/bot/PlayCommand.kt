package com.github.sipe90.sackbot.bot

import club.minnced.jda.reactor.toMono
import com.github.sipe90.sackbot.config.BotConfig
import com.github.sipe90.sackbot.service.AudioFileService
import com.github.sipe90.sackbot.service.AudioPlayerService
import net.dv8tion.jda.api.events.Event
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class PlayCommand(
    private val config: BotConfig,
    private val fileService: AudioFileService,
    private val playerService: AudioPlayerService
) : AbstractBotCommand() {

    override val commandPrefix = ""

    override fun canProcess(vararg command: String) = true

    override fun process(initiator: Event, vararg command: String): Mono<String> {
        val guild = getApplicableGuild(initiator)
        val voiceChannel = getVoiceChannel(initiator)

        if (guild == null || voiceChannel == null) return "Could not find guild or voice channel to perform the action".toMono()

        if (command.size != 1) {
            return "Invalid play command. Correct format is `${config.chat.commandPrefix}<soundName>`".toMono()
        }

        val audioFileName = command[0]

        return fileService.getAudioFilePathByName(audioFileName)
            .flatMap { playerService.playInChannel(it.toString(), voiceChannel) }
            .flatMap {
                return@flatMap if (it) "Playing sound file `$audioFileName` in voice channel `#${voiceChannel.name}`".toMono() else "Could not find a sound file with given name"
                    .toMono()
            }.switchIfEmpty(
                playerService.playInChannel(audioFileName, voiceChannel)
                    .flatMap {
                        return@flatMap if (it) "Playing sound file `$audioFileName` in voice channel `#${voiceChannel.name}`".toMono() else "Could not find any sounds with given name"
                            .toMono()
                    }
            )
    }
}