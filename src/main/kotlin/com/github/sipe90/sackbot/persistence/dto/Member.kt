package com.github.sipe90.sackbot.persistence.dto

import java.time.Instant

data class Member(
    val guildId: String,
    val userId: String,
    var entrySound: String?,
    var exitSound: String?,
    val createdBy: String,
    val created: Instant,
    var modifiedBy: String?,
    var modified: Instant?
)