package com.github.sipe90.sackbot.auth

import org.springframework.security.core.GrantedAuthority

/**
 * Describes user's authority over a guild
 */
class DiscordAuthority(
    val guildId: String,
    val roles: Set<String>,
    val isOwner: Boolean,
) : GrantedAuthority {
    override fun getAuthority() = null
}
