package com.github.sipe90.sackbot.audio.event

data class VolumeChangeEvent(override val guildId: String, val volume: Int) : GuildVoiceEvent()
