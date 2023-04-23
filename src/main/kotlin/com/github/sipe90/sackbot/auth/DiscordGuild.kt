package com.github.sipe90.sackbot.auth

import java.io.Serializable

data class DiscordGuild(
    val id: String,
    val name: String,
    val icon: String?,
    val owner: Boolean?,
    val permissions: Int?,
    val features: List<String>?,
): Serializable
