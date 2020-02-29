package com.github.sipe90.sackbot.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConfigurationProperties("sackbot.bot")
@ConstructorBinding
data class BotConfig(
        val token: String,
        val chat: ChatProps) {

    data class ChatProps(
            val enabled: Boolean,
            val allowDm: Boolean,
            val commandPrefix: String)
}