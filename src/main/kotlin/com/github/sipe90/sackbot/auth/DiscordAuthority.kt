package com.github.sipe90.sackbot.auth

import org.springframework.security.core.GrantedAuthority
import java.io.Serializable

/**
 * Describes user's authority over a guild
 */
data class DiscordAuthority(
    val guildId: String,
    val memberId: String,
    val roles: Set<String>,
    val isOwner: Boolean,
) : GrantedAuthority, Serializable {
    override fun getAuthority() = null
}
