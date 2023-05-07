package com.github.sipe90.sackbot.util

import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.User
import reactor.core.publisher.Mono
import reactor.util.context.Context

object ContextKey {
    const val INITIATOR = "INITIATOR"
}

fun createContext(user: User, member: Member?): Context =
    Context.of(
        ContextKey.INITIATOR,
        Initiator(
            guildId = member?.guild?.id,
            memberId = member?.id,
            user.id,
            name = member?.effectiveName ?: user.name,
            avatarUrl = member?.effectiveAvatarUrl ?: user.effectiveAvatarUrl,
        ),
    )

fun <T> withContext(monoFactory: Function1<Initiator?, Mono<out T>>): Mono<T> =
    Mono.deferContextual {
        monoFactory(it.getOrDefault(ContextKey.INITIATOR, null))
    }

data class Initiator(val guildId: String?, val memberId: String?, val userId: String, val name: String, val avatarUrl: String)
