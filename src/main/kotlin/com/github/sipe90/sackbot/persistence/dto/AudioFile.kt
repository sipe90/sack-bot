package com.github.sipe90.sackbot.persistence.dto

import org.dizitart.no2.IndexType
import org.dizitart.no2.objects.Id
import org.dizitart.no2.objects.Index
import org.dizitart.no2.objects.Indices
import java.time.Instant

@Indices(
    Index(value = "guildId", type = IndexType.NonUnique)
)
data class AudioFile(
    @Id val name: String,
    val extension: String?,
    val guildId: String,
    val createdBy: String,
    val created: Instant,
    val data: ByteArray
)