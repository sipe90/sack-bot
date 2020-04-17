package com.github.sipe90.sackbot.persistence.dto

import com.fasterxml.jackson.annotation.JsonView
import org.dizitart.no2.IndexType
import org.dizitart.no2.objects.Index
import org.dizitart.no2.objects.Indices
import java.time.Instant

@Indices(
    Index(value = "name", type = IndexType.NonUnique),
    Index(value = "guildId", type = IndexType.NonUnique)
)
data class AudioFile(
    @JsonView(API::class)
    var name: String,
    @JsonView(API::class)
    var extension: String?,
    @JsonView(API::class)
    var size: Int,
    @JsonView(API::class)
    val guildId: String,
    @JsonView(API::class)
    var tags: Set<String>,
    @JsonView(API::class)
    val createdBy: String,
    @JsonView(API::class)
    val created: Instant,
    @JsonView(API::class)
    var modifiedBy: String?,
    @JsonView(API::class)
    var modified: Instant?,
    var data: ByteArray
)