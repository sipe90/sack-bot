package com.github.sipe90.sackbot.audio.event

abstract class GuildVoiceEvent(open val guildId: String) {
    val type: String = this.javaClass.simpleName
}
