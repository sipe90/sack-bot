package com.github.sipe90.sackbot.service

import net.dv8tion.jda.api.entities.VoiceChannel

interface AudioPlayerService {

    fun playInChannel(identifier: String, voiceChannel: VoiceChannel)

    fun setVolume(guildId: String, volume: Int)

    fun getVolume(guildId: String): Int?
}