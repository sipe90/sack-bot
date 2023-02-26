package com.github.sipe90.sackbot.persistence

import com.github.sipe90.sackbot.persistence.dto.Member
import mu.KotlinLogging
import org.dizitart.kno2.filters.and
import org.dizitart.kno2.filters.eq
import org.dizitart.no2.objects.ObjectRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toFlux
import reactor.kotlin.core.publisher.toMono
import java.time.Instant

@Repository
class MemberRepository(val repository: ObjectRepository<Member>) {

    private val logger = KotlinLogging.logger {}

    fun getGuildMembers(guildId: String): Flux<Member> = repository.find(Member::guildId eq guildId).toFlux()

    fun getUserMemberships(userId: String): Flux<Member> = repository.find(Member::userId eq userId).toFlux()

    fun findOne(guildId: String, userId: String): Mono<Member> = Mono.defer {
        repository.find((Member::guildId eq guildId) and (Member::userId eq userId)).toMono()
            .flatMap {
                val member = it.firstOrDefault()
                if (member != null) {
                    logger.trace { "Found member: $member" }
                    Mono.just(member)
                } else {
                    logger.trace { "Member not found with: guildId=$guildId, userId=$userId" }
                    Mono.empty()
                }
            }
    }

    fun createMember(guildId: String, userId: String): Mono<Member> =
        insertMember(newMember(guildId, userId))

    fun updateMember(member: Member, userId: String): Mono<Member> = Mono.fromCallable {
        logger.trace { "Updating member: $member" }
        member.modified = Instant.now()
        member.modifiedBy = userId
        repository.update(findMemberFilter(member.guildId, member.userId), member, false)
        member
    }

    fun findOrCreate(guildId: String, userId: String): Mono<Member> =
        findOne(guildId, userId).switchIfEmpty(createMember(guildId, userId))

    private fun insertMember(member: Member) = Mono.fromCallable {
        logger.trace { "Saving new member: $member" }
        repository.insert(member)
        member
    }

    private fun newMember(guildId: String, userId: String) =
        Member(
            guildId,
            userId,
            null,
            null,
            "system",
            Instant.now(),
            null,
            null,
        )

    private fun findMemberFilter(guildId: String, userId: String) =
        (Member::guildId eq guildId) and (Member::userId eq userId)
}
