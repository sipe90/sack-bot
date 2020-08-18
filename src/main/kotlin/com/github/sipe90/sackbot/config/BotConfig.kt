package com.github.sipe90.sackbot.config

import net.dv8tion.jda.api.entities.Activity
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConfigurationProperties("sackbot.bot")
@ConstructorBinding
data class BotConfig(
        val token: String,
        val adminRole: String?,
        val activity: ActivityConfig,
        val chat: ChatConfig,
        val tts: TTSConfig,
        val voice: VoiceConfig,
        val upload: UploadConfig
) {

    data class ActivityConfig(
            val type: String,
            val text: String
    ) {
        fun getDiscordType(): Activity.ActivityType {
            return when (type.toLowerCase()) {
                "playing" -> Activity.ActivityType.DEFAULT
                "streaming" -> Activity.ActivityType.STREAMING
                "listening" -> Activity.ActivityType.LISTENING
                "watching" -> Activity.ActivityType.WATCHING
                // "custom" -> Activity.ActivityType.CUSTOM_STATUS
                else -> Activity.ActivityType.DEFAULT
            }
        }
    }

    data class ChatConfig(
            val enabled: Boolean,
            val allowDm: Boolean,
            val commandPrefix: String
    )

    data class TTSConfig(
            val maxLength: Int,
            val phrasesFile: String
    )

    data class VoiceConfig(
            val enabled: Boolean,
            val voices: Map<String, Voice>
    )

    data class Voice(
            val path: String,
            val substitutions: Map<String, String>?
    )

    data class UploadConfig(
            val sizeLimit: Int,
            val overrideExisting: Boolean
    )
}