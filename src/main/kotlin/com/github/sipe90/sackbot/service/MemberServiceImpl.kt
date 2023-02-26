package com.github.sipe90.sackbot.service

import com.github.sipe90.sackbot.exception.NotFoundException
import com.github.sipe90.sackbot.persistence.MemberRepository
import com.github.sipe90.sackbot.persistence.dto.Member
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Service
class MemberServiceImpl(
    private val memberRepository: MemberRepository,
    private val audioFileService: AudioFileService,
) : MemberService {

    override fun getMember(guildId: String, userId: String): Mono<Member> =
        memberRepository.findOrCreate(guildId, userId)

    override fun getUserMemberships(userId: String): Flux<Member> =
        memberRepository.getUserMemberships(userId)

    override fun setMemberEntrySound(guildId: String, userId: String, name: String?): Mono<Unit> {
        return updateMember(guildId, userId, name) { member ->
            member.entrySound = name
            member
        }
    }

    override fun setMemberExitSound(guildId: String, userId: String, name: String?): Mono<Unit> {
        return updateMember(guildId, userId, name) { member ->
            member.exitSound = name
            member
        }
    }

    private fun updateMember(
        guildId: String,
        userId: String,
        name: String?,
        mutator: (member: Member) -> Member,
    ): Mono<Unit> {
        return memberRepository.findOrCreate(guildId, userId).flatMap { member ->
            if (name === null) {
                memberRepository.updateMember(mutator.invoke(member), userId)
            } else {
                audioFileService.audioFileExists(guildId, name).flatMap { exists ->
                    if (exists) {
                        memberRepository.updateMember(mutator.invoke(member), userId)
                    } else {
                        Mono.error(NotFoundException("No sound found with name \"$name\""))
                    }
                }
            }
        }.then(Mono.empty())
    }
}
