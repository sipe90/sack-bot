package com.github.sipe90.sackbot.handler

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.sipe90.sackbot.audio.event.GuildVoiceEventEmitter
import com.github.sipe90.sackbot.auth.DiscordAuthority
import com.github.sipe90.sackbot.service.AudioPlayerService
import com.github.sipe90.sackbot.service.JDAService
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.stereotype.Component
import org.springframework.web.reactive.socket.CloseStatus
import org.springframework.web.reactive.socket.WebSocketHandler
import org.springframework.web.reactive.socket.WebSocketSession
import org.springframework.web.util.UriComponentsBuilder
import reactor.core.publisher.Mono
import java.net.URI

@Component
class AudioEventsHandler(
    private val objectMapper: ObjectMapper,
    private val jdaService: JDAService,
    private val audioPlayerService: AudioPlayerService,
    private val eventEmitter: GuildVoiceEventEmitter
) : WebSocketHandler {

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
                    Mono.just<Any>(buildInitialState(guildId))
                        .mergeWith(eventEmitter.subscribe(guildId))
                        .map { objectMapper.writeValueAsString(it) }
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

    private fun buildInitialState(guildId: String): InitialVoiceState {
        val guild = jdaService.getGuild(guildId)
        val volume = audioPlayerService.getVolume(guildId)
        return InitialVoiceState(guild?.selfMember?.voiceState?.channel?.name, volume)
    }

    data class InitialVoiceState(val currentChannel: String?, val volume: Int) {
        val type: String = this.javaClass.simpleName
    }
}
