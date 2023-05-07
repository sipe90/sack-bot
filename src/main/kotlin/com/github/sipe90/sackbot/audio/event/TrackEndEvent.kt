package com.github.sipe90.sackbot.audio.event

data class TrackEndEvent(
    override val guildId: String,
    override val initiatorName: String?,
    override val initiatorAvatar: String?,
    val track: String,
) : GuildVoiceEvent()
