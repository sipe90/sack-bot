package com.github.sipe90.sackbot.handler.dto

data class SettingsDTO(
    val upload: UploadSettings,
) {
    data class UploadSettings(
        val sizeLimit: Int,
        val overrideExisting: Boolean,
    )
}
