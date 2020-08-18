package com.github.sipe90.sackbot.handler.dto


data class SettingsDTO(
        val tts: TTSSettings,
        val voice: VoiceSettings,
        val upload: UploadSettings
) {
    data class TTSSettings(
            val enabled: Boolean,
            val voices: Set<String>,
            val maxLength: Int,
            val randomEnabled: Boolean
    )

    data class VoiceSettings(
            val enabled: Boolean,
            val voices: Map<String, Set<String>>
    )

    data class UploadSettings(
            val sizeLimit: Int,
            val overrideExisting: Boolean
    )
}