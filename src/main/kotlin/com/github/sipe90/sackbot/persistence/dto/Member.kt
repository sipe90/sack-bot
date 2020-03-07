package com.github.sipe90.sackbot.persistence.dto

data class Member(
    val guildId: String,
    val userId: String,
    var entrySound: String?,
    var exitSound: String?
)