package com.github.sipe90.sackbot.controller

import com.github.sipe90.sackbot.auth.DiscordUser
import com.github.sipe90.sackbot.persistence.MemberRepository
import com.github.sipe90.sackbot.persistence.dto.Member
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux

@RestController
class UserController(private val memberRepository: MemberRepository) {

    @GetMapping("/me")
    fun userInfo(@AuthenticationPrincipal principal: DiscordUser): Flux<Member> {
        return memberRepository.getUserMembers(principal.getId())
    }
}