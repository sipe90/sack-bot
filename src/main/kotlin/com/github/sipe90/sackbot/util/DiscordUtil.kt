package com.github.sipe90.sackbot.util

import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.VoiceChannel

fun getGuild(user: User): Guild? = user.mutualGuilds.firstOrNull { guild ->
    guild.voiceChannels.flatMap { vc -> vc.members }.map { m -> m.id }.contains(user.id)
}

fun getVoiceChannel(user: User): VoiceChannel? = user.mutualGuilds
    .flatMap { guild -> guild.voiceChannels }
    .find { vc -> vc.members.map { m -> m.id }.contains(user.id) }

fun getVoiceChannel(guild: Guild, user: User): VoiceChannel? = guild.voiceChannels
    .find { vc -> vc.members.map { m -> m.id }.contains(user.id) }