package com.github.sipe90.sackbot.persistence

import com.github.sipe90.sackbot.persistence.dto.Member
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
class MemberRepositoryExtensionImpl(private val template: ReactiveMongoTemplate) : MemberRepositoryExtension {
    override fun findMember(guildId: String, userId: String): Mono<Member> {
        return template.findOne(Query(Criteria.where("guildId").`is`(guildId).and("userId").`is`(userId)), Member::class.java)
    }

    override fun getUserMemberships(userId: String): Flux<Member> {
        return template.find(Query(Criteria.where("userId").`is`(userId)), Member::class.java)
    }
}
