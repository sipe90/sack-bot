package com.github.sipe90.sackbot.util

import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel

fun getGuild(user: User): Guild? =
    if (user.mutualGuilds.size == 1) {
        user.mutualGuilds[0]
    } else {
        user.mutualGuilds.firstOrNull { guild ->
            guild.voiceChannels.flatMap { vc -> vc.members }.map { m -> m.id }.contains(user.id)
        }
    }

fun getMember(user: User): Member? = getGuild(user)?.getMember(user)

fun getVoiceChannel(user: User): VoiceChannel? =
    user.mutualGuilds
        .flatMap { guild -> guild.voiceChannels }
        .find { vc -> vc.members.map { m -> m.id }.contains(user.id) }

fun getVoiceChannel(
    guild: Guild,
    user: User,
): VoiceChannel? =
    guild.voiceChannels
        .find { vc -> vc.members.map { m -> m.id }.contains(user.id) }
