package com.github.sipe90.sackbot.bot.command

import com.github.sipe90.sackbot.SackException
import com.github.sipe90.sackbot.service.AudioPlayerService
import com.github.sipe90.sackbot.util.getVoiceChannel
import com.sedmelluq.discord.lavaplayer.track.AudioItem
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.kotlin.core.publisher.toMono

@Component
class UrlCommand(private val playerService: AudioPlayerService) : BotCommand() {

    final override val commandName = "url"

    final override val commandData =
        Commands.slash("url", "Play a sound from an URL (Youtube, Twitch, BandCamp, GetYarn, Generic HTTP(S) source)")
            .addOption(OptionType.STRING, "url", "URL to load the audio from", true)

    override fun process(initiator: SlashCommandInteractionEvent): Flux<String> = Flux.defer {
        val voiceChannel = getVoiceChannel(initiator.user)
            ?: return@defer "Could not find guild or voice channel to perform the action".toMono()

        val soundOpt = initiator.getOption("url") ?: throw SackException("Url option missing from url command")
        val url = soundOpt.asString

        playerService.playUrlInChannel(url, voiceChannel, null)
            .map { "Playing ${getItemString(it) ?: url} in voice channel `#${voiceChannel.name}`" }
            .onErrorReturn("Could not play sound from url `$url`")
    }

    private fun getItemString(item: AudioItem): String? {
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