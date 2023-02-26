package com.github.sipe90.sackbot.service

import com.github.sipe90.sackbot.persistence.dto.Member
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface MemberService {

    fun getMember(guildId: String, userId: String): Mono<Member>

    fun getUserMemberships(userId: String): Flux<Member>

    fun setMemberEntrySound(guildId: String, userId: String, name: String?): Mono<Unit>

    fun setMemberExitSound(guildId: String, userId: String, name: String?): Mono<Unit>
}
