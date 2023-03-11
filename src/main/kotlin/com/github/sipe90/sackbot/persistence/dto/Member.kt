package com.github.sipe90.sackbot.persistence.dto

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.CompoundIndex
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

@Document("members")
@CompoundIndex(name = "userId_guildId", def = "{'userId' : 1, 'guildId': 1}", unique = true)
data class Member(
    @Id
    var id: String?,
    val userId: String,
    val guildId: String,
    var entrySound: String?,
    var exitSound: String?,
    val createdBy: String,
    val created: Instant,
    var modifiedBy: String?,
    var modified: Instant?,
) {
    constructor(userId: String, guildId: String) : this(
        id = null,
        userId = userId,
        guildId = guildId,
        entrySound = null,
        exitSound = null,
        createdBy = "system",
        created = Instant.now(),
        modifiedBy = null,
        modified = null,
    )
}
