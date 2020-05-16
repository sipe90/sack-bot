package com.github.sipe90.sackbot.bot.command

import com.github.sipe90.sackbot.service.AudioFileService
import com.github.sipe90.sackbot.service.AudioPlayerService
import com.github.sipe90.sackbot.util.getUser
import com.github.sipe90.sackbot.util.getVoiceChannel
import net.dv8tion.jda.api.events.Event
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.kotlin.core.publisher.toMono
import java.lang.NumberFormatException

@Component
class RandomCommand(private val fileService: AudioFileService, private val playerService: AudioPlayerService) :
    BotCommand {

    override val commandPrefix = "rnd"

    override fun process(initiator: Event, vararg command: String): Flux<String> = Flux.defer {
        val voiceChannel = getVoiceChannel(initiator)
            ?: return@defer "Could not find guild or voice channel to perform the action".toMono()
        val user = getUser(initiator) ?: return@defer "Could not find user".toMono()

        val volumeStr = command[1]
        val volume: Int? =
                if (command.size == 2) {
                    try {
                        Integer.parseInt(volumeStr).coerceIn(1, 100)
                    } catch (e: NumberFormatException) {
                        return@defer "Invalid volume given. Value must be a number.".toMono()
                    }
                } else null

        return@defer fileService.randomAudioFile(voiceChannel.guild.id, user.id)
            .flatMap { audioFile -> playerService.playAudioInChannel(audioFile.name, voiceChannel, volume).map { audioFile } }
            .map { "Playing random sound file `${it.name}` in voice channel `#${voiceChannel.name}`" }
    }
}