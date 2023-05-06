package com.github.sipe90.sackbot.audio.event

data class VoiceChannelEvent(override val guildId: String, val channelLeft: String?, val channelJoined: String?) : GuildVoiceEvent(guildId)
