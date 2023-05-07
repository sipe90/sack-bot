package com.github.sipe90.sackbot.audio.event

data class VolumeChangeEvent(
    override val guildId: String,
    override val initiatorName: String?,
    override val initiatorAvatar: String?,
    val volume: Int,
) : GuildVoiceEvent()
