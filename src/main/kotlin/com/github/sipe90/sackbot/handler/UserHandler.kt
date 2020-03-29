package com.github.sipe90.sackbot.handler

import com.github.sipe90.sackbot.auth.DiscordUser
import com.github.sipe90.sackbot.config.BotConfig
import com.github.sipe90.sackbot.handler.dto.GuildMemberDTO
import com.github.sipe90.sackbot.persistence.MemberRepository
import com.github.sipe90.sackbot.service.JDAService
import net.dv8tion.jda.api.entities.Role
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.ServerResponse.ok
import org.springframework.web.reactive.function.server.body
import reactor.core.publisher.Mono

@Component
class UserHandler(
    private val config: BotConfig,
    private val memberRepository: MemberRepository,
    private val jdaService: JDAService
) {

    fun userInfo(request: ServerRequest, principal: DiscordUser): Mono<ServerResponse> {
        return ok()
            .body(memberRepository.getUserMembers(principal.getId()))
    }

    fun mutualGuilds(request: ServerRequest, principal: DiscordUser): Mono<ServerResponse> {
        val userId = principal.getId()
        val user = jdaService.getUser(userId)

        return ok()
            .bodyValue(jdaService.getMutualGuilds(principal.getId()).map {
                Guild(
                    it.id,
                    it.name,
                    it.iconUrl,
                    hasAdminAccess(it, user!!),
                    memberRoles(it, user)
                )
            })
    }

    fun guildMembers(request: ServerRequest, principal: DiscordUser): Mono<ServerResponse> {
        val guildId = request.pathVariable("guildId")

        return ok().body(
            Mono.fromCallable {
                val guild = jdaService.getGuild(guildId) ?: throw RuntimeException("Could not get guild")
                guild.members.map { GuildMemberDTO(it.id, it.effectiveName) }
            }
        )
    }

    private fun memberRoles(
        guild: net.dv8tion.jda.api.entities.Guild,
        user: net.dv8tion.jda.api.entities.User
    ): List<String> {
        val member = guild.getMember(user)
        return member!!.roles.map(Role::getName)
    }

    private fun hasAdminAccess(
        guild: net.dv8tion.jda.api.entities.Guild,
        user: net.dv8tion.jda.api.entities.User
    ): Boolean =
        guild.ownerId == user.id ||
            (config.adminRole != null &&
                guild.getMember(user)?.roles?.any { it.name == config.adminRole } ?: false)

    data class Guild(
        val id: String,
        val name: String,
        val iconUrl: String?,
        val isAdmin: Boolean,
        val roles: List<String>
    )
}