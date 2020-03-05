package com.github.sipe90.sackbot.bot

import com.github.sipe90.sackbot.SackException
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.VoiceChannel
import net.dv8tion.jda.api.events.Event
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent

abstract class AbstractBotCommand : BotCommand {

    override fun canProcess(vararg command: String) = command.isNotEmpty() && command[0] == commandPrefix

    protected fun getApplicableGuild(event: Event): Guild? {
        return when (event) {
            is PrivateMessageReceivedEvent ->
                event.author.mutualGuilds.first { guild ->
                    guild.voiceChannels.flatMap { vc -> vc.members }.map { m -> m.id }.contains(event.author.id)
                }
            is GuildMessageReceivedEvent -> event.guild
            else -> throw SackException("Invalid event")
        }
    }

    protected fun getVoiceChannel(event: Event): VoiceChannel? {
        return when (event) {
            is PrivateMessageReceivedEvent -> event.author.mutualGuilds
                .flatMap { guild -> guild.voiceChannels }
                .find { vc -> vc.members.map { m -> m.id }.contains(event.author.id) }
            is GuildMessageReceivedEvent -> event.member?.voiceState?.channel
            else -> throw SackException("Invalid event")
        }
    }
}