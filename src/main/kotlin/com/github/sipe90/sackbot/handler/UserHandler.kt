package com.github.sipe90.sackbot.handler

import com.github.sipe90.sackbot.auth.DiscordUser
import com.github.sipe90.sackbot.persistence.MemberRepository
import com.github.sipe90.sackbot.service.JDAService
import net.dv8tion.jda.api.entities.Role
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.body
import reactor.core.publisher.Mono

@Component
class UserHandler(private val memberRepository: MemberRepository, private val jdaService: JDAService) {

    fun userInfo(request: ServerRequest, principal: DiscordUser): Mono<ServerResponse> {
        return ServerResponse.ok()
            .body(memberRepository.getUserMembers(principal.getId()))
    }

    fun mutualGuilds(request: ServerRequest, principal: DiscordUser): Mono<ServerResponse> {
        val userId = principal.getId()
        val user = jdaService.getUser(userId)

        return ServerResponse.ok()
            .bodyValue(jdaService.getMutualGuilds(principal.getId()).map {
                Guild(
                    it.id,
                    it.name,
                    it.iconUrl,
                    it.ownerId == principal.getId(),
                    memberRoles(it, user!!)
                )
            })
    }

    private fun memberRoles(
        guild: net.dv8tion.jda.api.entities.Guild,
        user: net.dv8tion.jda.api.entities.User
    ): List<String> {
        val member = guild.getMember(user)
        return member!!.roles.map(Role::getName)
    }

    data class Guild(
        val id: String,
        val name: String,
        val iconUrl: String?,
        val owner: Boolean,
        val roles: List<String>
    )
}