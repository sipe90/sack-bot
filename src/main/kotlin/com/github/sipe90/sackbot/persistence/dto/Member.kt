package com.github.sipe90.sackbot.persistence.dto

import java.time.Instant

data class Member(
    val guildId: String,
    val userId: String,
    var entrySound: String?,
    var exitSound: String?,
    // FIXME: Set not nullable
    val createdBy: String?,
    // FIXME: Set not nullable
    val created: Instant?,
    var modifiedBy: String?,
    var modified: Instant?
)