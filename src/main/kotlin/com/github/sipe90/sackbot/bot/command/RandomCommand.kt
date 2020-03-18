package com.github.sipe90.sackbot.bot.command

import club.minnced.jda.reactor.toMono
import com.github.sipe90.sackbot.service.AudioFileService
import com.github.sipe90.sackbot.service.AudioPlayerService
import com.github.sipe90.sackbot.util.getUser
import com.github.sipe90.sackbot.util.getVoiceChannel
import net.dv8tion.jda.api.events.Event
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class RandomCommand(private val fileService: AudioFileService, private val playerService: AudioPlayerService) :
    BotCommand {

    override val commandPrefix = "rnd"

    override fun process(initiator: Event, vararg command: String): Mono<String> = Mono.defer {
        val voiceChannel = getVoiceChannel(initiator)
            ?: return@defer "Could not find guild or voice channel to perform the action".toMono()
        val user = getUser(initiator) ?: return@defer "Could not find user".toMono()
        val paths = fileService.getAudioFiles(voiceChannel.guild.id, user.id)
        paths
            .count()
            .map { (1..it).random() }
            .flatMap { paths.take(it).last() }
            .flatMap { audioFile -> playerService.playAudioInChannel(audioFile.name, voiceChannel).map { audioFile } }
            .map { "Playing random sound file `${it.name}` in voice channel `#${voiceChannel.name}`" }
    }
}