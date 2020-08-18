package com.github.sipe90.sackbot.persistence.dto.projection

import java.time.Instant

data class LightAudioFile(
        var name: String,
        var extension: String?,
        var size: Int,
        val guildId: String,
        var tags: Set<String>,
        val createdBy: String,
        val created: Instant,
        var modifiedBy: String?,
        var modified: Instant?
)