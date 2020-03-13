package com.github.sipe90.sackbot.util

import com.github.sipe90.sackbot.SackException
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.VoiceChannel
import net.dv8tion.jda.api.events.Event
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent

fun getVoiceChannel(event: Event): VoiceChannel? =
    when (event) {
        is PrivateMessageReceivedEvent -> event.author.mutualGuilds
            .flatMap { guild -> guild.voiceChannels }
            .find { vc -> vc.members.map { m -> m.id }.contains(event.author.id) }
        is GuildMessageReceivedEvent -> event.member?.voiceState?.channel
        else -> throw SackException("Invalid event")
    }

fun getApplicableGuild(event: Event): Guild? =
    when (event) {
        is PrivateMessageReceivedEvent ->
            event.author.mutualGuilds.firstOrNull { guild ->
                guild.voiceChannels.flatMap { vc -> vc.members }.map { m -> m.id }.contains(event.author.id)
            }
        is GuildMessageReceivedEvent -> event.guild
        else -> throw SackException("Invalid event")
    }

fun getUser(event: Event): User? =
    when (event) {
        is PrivateMessageReceivedEvent -> event.author
        is GuildMessageReceivedEvent -> event.member?.user
        else -> throw SackException("Invalid event")
    }