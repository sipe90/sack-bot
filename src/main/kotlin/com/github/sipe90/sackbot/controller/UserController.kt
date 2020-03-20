package com.github.sipe90.sackbot.controller

import com.github.sipe90.sackbot.auth.DiscordUser
import com.github.sipe90.sackbot.persistence.MemberRepository
import com.github.sipe90.sackbot.persistence.dto.Member
import com.github.sipe90.sackbot.service.JDAService
import net.dv8tion.jda.api.entities.Role
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import reactor.core.publisher.toFlux

@RestController
class UserController(private val memberRepository: MemberRepository, private val jdaService: JDAService) {

    @GetMapping("/me")
    fun userInfo(@AuthenticationPrincipal principal: DiscordUser): Flux<Member> {
        return memberRepository.getUserMembers(principal.getId())
    }

    @GetMapping("/guilds")
    fun mutualGuilds(@AuthenticationPrincipal principal: DiscordUser): Flux<Guild> {
        val user = jdaService.getUser(principal.getId())
        return jdaService.getMutualGuilds(principal.getId()).map {
            Guild(
                it.id,
                it.name,
                it.iconUrl,
                it.ownerId == principal.getId(),
                memberRoles(it, user!!)
            )
        }.toFlux()
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