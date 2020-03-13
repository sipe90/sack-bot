package com.github.sipe90.sackbot.auth

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.oauth2.core.user.DefaultOAuth2User

/**
 * https://discordapp.com/developers/docs/resources/user#user-object
 */
class DiscordUser(
    authorities: Collection<GrantedAuthority>,
    attributes: Map<String, Any>,
    nameAttributeKey: String
) : DefaultOAuth2User(authorities, attributes, nameAttributeKey) {

    // Identity scope

    fun getId(): String = attributes["id"] as String
    fun getUsername(): String = attributes["username"] as String
    fun getDiscriminator(): String = attributes["discriminator"] as String
    fun getAvatar(): String? = attributes["avatar"] as String?
    fun isBot(): Boolean? = attributes["bot"] as Boolean?
    fun isSystem(): Boolean? = attributes["system"] as Boolean?
    fun isMfaEnabled(): Boolean? = attributes["mfa_enabled"] as Boolean?
    fun getLocale(): String? = attributes["locale"] as String?
    fun getFlags(): Int? = attributes["flags"] as Int?
    fun getPremiumType(): Int? = attributes["premium_type"] as Int?

    // Email scope

    fun isVerified(): Boolean? = attributes["verified"] as Boolean?
    fun getEmail(): String? = attributes["email"] as String?
}