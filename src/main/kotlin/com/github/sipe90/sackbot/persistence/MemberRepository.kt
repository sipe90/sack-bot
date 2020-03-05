package com.github.sipe90.sackbot.persistence

import club.minnced.jda.reactor.toMono
import com.github.sipe90.sackbot.persistence.dto.Member
import org.dizitart.kno2.filters.and
import org.dizitart.kno2.filters.eq
import org.dizitart.no2.objects.ObjectRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
class MemberRepository(val repository: ObjectRepository<Member>) {

    fun findOne(guildId: String, userId: String): Mono<Member> =
        repository.find((Member::guildId eq guildId) and (Member::userId eq userId)).toMono()
            .flatMap {
                val member = it.firstOrDefault()
                return@flatMap if (member == null) Mono.empty<Member>() else Mono.just(member)
            }

    fun createMember(guildId: String, userId: String) = saveMember(Member(guildId, userId, null, null))

    fun saveMember(member: Member): Mono<Member> {
        repository.insert(member)
        return Mono.just(member)
    }

    fun findOrCreate(guildId: String, userId: String) =
        findOne(guildId, userId).switchIfEmpty(createMember(guildId, userId))
}