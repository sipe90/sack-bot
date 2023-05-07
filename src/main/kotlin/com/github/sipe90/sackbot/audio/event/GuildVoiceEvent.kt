package com.github.sipe90.sackbot.audio.event

import com.fasterxml.jackson.annotation.JsonIgnore

abstract class GuildVoiceEvent {

    @get:JsonIgnore
    abstract val guildId: String

    abstract val initiatorName: String?

    abstract val initiatorAvatar: String?

    val type: String = this.javaClass.simpleName
}
