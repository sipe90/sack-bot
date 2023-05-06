package com.github.sipe90.sackbot.handler

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.sipe90.sackbot.audio.event.GuildVoiceEventEmitter
import com.github.sipe90.sackbot.auth.DiscordAuthority
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.stereotype.Component
import org.springframework.web.reactive.socket.CloseStatus
import org.springframework.web.reactive.socket.WebSocketHandler
import org.springframework.web.reactive.socket.WebSocketSession
import org.springframework.web.util.UriComponentsBuilder
import reactor.core.publisher.Mono
import java.net.URI
import java.util.logging.Level

@Component
class AudioEventsHandler(private val objectMapper: ObjectMapper, private val eventEmitter: GuildVoiceEventEmitter) : WebSocketHandler {

    override fun handle(session: WebSocketSession): Mono<Void> {
        val guildId = getGuildId(session.handshakeInfo.uri)
            ?: return session.send(
                session.close(CloseStatus.POLICY_VIOLATION.withReason("Missing guildId parameter")).thenMany(Mono.empty()),
            )

        return session.send(
            hasGuildAccess(guildId).flatMapMany { hasAccess ->
                if (!hasAccess) {
                    session.close(CloseStatus.POLICY_VIOLATION.withReason("Forbidden")).thenMany(Mono.empty())
                } else {
                    eventEmitter.subscribe(guildId)
                        .map { objectMapper.writeValueAsString(it) }
                        .log(null, Level.FINE)
                        .map { session.textMessage(it) }
                }
            },
        )
    }

    private fun getGuildId(uri: URI): String? =
        UriComponentsBuilder.fromUri(uri).build()
            .queryParams.getFirst("guildId")

    private fun hasGuildAccess(guildId: String) = getAuthorities().map { it.any { auth -> auth.guildId == guildId } }
    private fun getAuthorities(): Mono<Collection<DiscordAuthority>> {
        return ReactiveSecurityContextHolder.getContext().map {
            (it.authentication as OAuth2AuthenticationToken).authorities as Collection<DiscordAuthority>
        }
    }
}
