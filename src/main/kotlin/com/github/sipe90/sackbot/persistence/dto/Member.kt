package com.github.sipe90.sackbot.persistence.dto

data class Member(
    val userId: String,
    val guildId: String,
    var entrySound: String?,
    val exitSound: String?
)