package com.github.sipe90.sackbot.persistence.dto

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.CompoundIndex
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

@Document("audio_files")
@CompoundIndex(name = "name_guildId", def = "{'name' : 1, 'guildId': 1}", unique = true)
data class AudioFile(
    @Id
    var id: String?,
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
) {
    constructor(name: String, extension: String?, size: Int, guildId: String, createdBy: String, data: ByteArray) : this(
        id = null,
        name = name,
        extension = extension,
        size = size,
        guildId = guildId,
        tags = emptySet(),
        createdBy = createdBy,
        created = Instant.now(),
        modifiedBy = null,
        modified = null,
        data = data,
    )
}
