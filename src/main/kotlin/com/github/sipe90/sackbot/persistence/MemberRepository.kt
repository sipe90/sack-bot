package com.github.sipe90.sackbot.persistence

import com.github.sipe90.sackbot.persistence.dto.Member
import org.dizitart.kno2.filters.and
import org.dizitart.kno2.filters.eq
import org.dizitart.no2.objects.ObjectRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.toFlux
import reactor.core.publisher.toMono
import java.time.Instant

@Repository
class MemberRepository(val repository: ObjectRepository<Member>) {

    final val logger = LoggerFactory.getLogger(javaClass)

    fun getGuildMembers(guildId: String): Flux<Member> = repository.find(Member::guildId eq guildId).toFlux()

    fun getUserMembers(userId: String): Flux<Member> = repository.find(Member::userId eq userId).toFlux()

    fun findOne(guildId: String, userId: String): Mono<Member> = Mono.defer {
        repository.find((Member::guildId eq guildId) and (Member::userId eq userId)).toMono()
            .flatMap {
                val member = it.firstOrDefault()
                if (member != null) {
                    logger.trace("Found member:  {}", member)
                    return@flatMap Mono.just(member)
                } else {
                    logger.trace("Member not found with: guildId={}, userId={}", guildId, userId)
                    return@flatMap Mono.empty<Member>()
                }
            }
    }

    fun createMember(guildId: String, userId: String): Mono<Member> =
        insertMember(
            Member(
                guildId,
                userId,
                null,
                null,
                "system",
                Instant.now(),
                null,
                null
            )
        )

    fun updateMember(member: Member, userId: String): Mono<Member> = Mono.fromCallable {
        logger.trace("Updating member: {}", member)
        member.modified = Instant.now()
        member.modifiedBy = userId
        repository.update(findMemberFilter(member.guildId, member.userId), member, false)
        member
    }

    fun findOrCreate(guildId: String, userId: String): Mono<Member> =
        findOne(guildId, userId).switchIfEmpty(createMember(guildId, userId))

    private fun insertMember(member: Member) = Mono.fromCallable {
        logger.trace("Saving new member: {}", member)
        repository.insert(member)
        member
    }

    private fun findMemberFilter(guildId: String, userId: String) =
        (Member::guildId eq guildId) and (Member::userId eq userId)
}