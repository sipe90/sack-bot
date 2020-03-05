package com.github.sipe90.sackbot.bot

import club.minnced.jda.reactor.toMono
import com.github.sipe90.sackbot.service.AudioFileService
import com.github.sipe90.sackbot.service.AudioPlayerService
import net.dv8tion.jda.api.events.Event
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import reactor.util.function.Tuples

@Component
class RandomCommand(private val fileService: AudioFileService, private val playerService: AudioPlayerService) :
    AbstractBotCommand() {

    override val commandPrefix = "rnd"

    override fun process(initiator: Event, vararg command: String): Mono<String> {
        val voiceChannel = getVoiceChannel(initiator)
            ?: return "Could not find guild or voice channel to perform the action".toMono()
        val paths = fileService.getAudioFiles()
        return paths
            .count()
            .map { (0..it).random() }
            .flatMap { paths.take(it).last() }
            .flatMap { fileService.getAudioFilePathByName(it).map { p -> Tuples.of(it, p) } }
            .flatMap { playerService.playInChannel(it.t2.toString(), voiceChannel).map { p -> Tuples.of(it.t1, p) } }
            .flatMap {
                "Playing random sound file `${it.t1}` in voice channel `#${voiceChannel.name}`".toMono()
            }
    }
}