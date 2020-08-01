package com.github.sipe90.sackbot.bot.command

import com.github.sipe90.sackbot.config.BotConfig
import com.github.sipe90.sackbot.service.AudioPlayerService
import com.github.sipe90.sackbot.util.getGuild
import com.github.sipe90.sackbot.util.getVoiceChannel
import com.sedmelluq.discord.lavaplayer.track.AudioItem
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import net.dv8tion.jda.api.events.Event
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.kotlin.core.publisher.toMono

@Component
class UrlCommand(
        private val config: BotConfig,
        private val playerService: AudioPlayerService
) : BotCommand() {

    override val commandPrefix = "url"

    override fun canProcess(vararg command: String) = true

    override fun process(initiator: Event, vararg command: String): Flux<String> = Flux.defer {
        val guild = getGuild(initiator)
        val voiceChannel = getVoiceChannel(initiator)

        if (guild == null || voiceChannel == null) return@defer "Could not find guild or voice channel to perform the action".toMono()

        if (command.size > 3) {
            return@defer "Invalid url command. Correct format is `${config.chat.commandPrefix}url <url> [volume]`".toMono()
        }

        val url = command[1]

        val volume: Int? =
                if (command.size == 3) {
                    val volumeStr = command[2]
                    try {
                        Integer.parseInt(volumeStr).coerceIn(1, 100)
                    } catch (e: NumberFormatException) {
                        return@defer "Invalid volume given. Value must be a number".toMono()
                    }
                } else null

        playerService.playUrlInChannel(url, voiceChannel, volume)
                .map { "Playing ${getItemString(it) ?: url} in voice channel `#${voiceChannel.name}`" }
                .onErrorReturn("Could not find a sound source with given url")
    }

    fun getItemString(item: AudioItem): String? {
        return when (item) {
            is AudioTrack -> {
                val author = item.info.author
                val title = item.info.title
                if (author == null && title == null) return null
                if (author == null) return "´$title`"
                return "track `$author - $title`"
            }
            is AudioPlaylist -> {
                val playlistString = "playlist ´$item.name´"
                if (item.selectedTrack == null) return playlistString
                val trackString = getItemString(item) ?: return playlistString
                return "$playlistString, $trackString"
            }
            else -> null
        }
    }
}