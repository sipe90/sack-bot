package com.github.sipe90.sackbot.persistence

import com.github.sipe90.sackbot.persistence.dto.Member
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Repository

@Repository
interface MemberRepository : ReactiveMongoRepository<Member, String>, MemberRepositoryExtension
