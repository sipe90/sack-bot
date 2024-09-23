package com.github.sipe90.sackbot.persistence

import com.github.sipe90.sackbot.persistence.dto.Member
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface MemberRepositoryExtension {
    fun findMember(
        guildId: String,
        userId: String,
    ): Mono<Member>

    fun getUserMemberships(userId: String): Flux<Member>
}
