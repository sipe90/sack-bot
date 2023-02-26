package com.github.sipe90.sackbot.persistence.dto

import org.dizitart.no2.IndexType
import org.dizitart.no2.objects.Index
import org.dizitart.no2.objects.Indices
import java.time.Instant

@Indices(
    Index(value = "name", type = IndexType.NonUnique),
    Index(value = "guildId", type = IndexType.NonUnique),
)
data class AudioFile(
    var name: String,
    var extension: String?,
    var size: Int,
    val guildId: String,
    var tags: Set<String>,
    val createdBy: String,
    val created: Instant,
    var modifiedBy: String?,
    var modified: Instant?,
    var data: ByteArray,
)
