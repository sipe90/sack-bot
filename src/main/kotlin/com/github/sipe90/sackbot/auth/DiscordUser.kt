package com.github.sipe90.sackbot.auth

import com.fasterxml.jackson.annotation.JsonView
import com.github.sipe90.sackbot.persistence.dto.API
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

    @JsonView(API::class)
    fun getId(): String = attributes["id"] as String

    @JsonView(API::class)
    fun getUsername(): String = attributes["username"] as String

    @JsonView(API::class)
    fun getDiscriminator(): String = attributes["discriminator"] as String

    @JsonView(API::class)
    fun getAvatar(): String? = attributes["avatar"] as String?

    @JsonView(API::class)
    fun isBot(): Boolean? = attributes["bot"] as Boolean?

    @JsonView(API::class)
    fun isSystem(): Boolean? = attributes["system"] as Boolean?

    @JsonView(API::class)
    fun isMfaEnabled(): Boolean? = attributes["mfa_enabled"] as Boolean?

    @JsonView(API::class)
    fun getLocale(): String? = attributes["locale"] as String?

    @JsonView(API::class)
    fun getFlags(): Int? = attributes["flags"] as Int?

    @JsonView(API::class)
    fun getPremiumType(): Int? = attributes["premium_type"] as Int?

    // Email scope

    @JsonView(API::class)
    fun isVerified(): Boolean? = attributes["verified"] as Boolean?

    @JsonView(API::class)
    fun getEmail(): String? = attributes["email"] as String?

    // Guilds scope

    @JsonView(API::class)
    fun getGuilds(): List<String> = attributes["guilds"] as List<String>
}