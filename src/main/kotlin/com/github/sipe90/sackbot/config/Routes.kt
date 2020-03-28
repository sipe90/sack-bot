package com.github.sipe90.sackbot.config

import com.github.sipe90.sackbot.auth.DiscordUser
import com.github.sipe90.sackbot.handler.AudioHandler
import com.github.sipe90.sackbot.handler.TTSHandler
import com.github.sipe90.sackbot.handler.UserHandler
import com.github.sipe90.sackbot.handler.VoiceHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.web.reactive.function.server.RouterFunctions
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.ServerResponse.status
import org.springframework.web.reactive.function.server.router
import reactor.core.publisher.Mono
import reactor.kotlin.core.util.function.component1
import reactor.kotlin.core.util.function.component2
import reactor.util.function.Tuple2
import reactor.util.function.Tuples

@Configuration
class Routes(
    private val userHandler: UserHandler,
    private val audioHandler: AudioHandler,
    private val voiceHandler: VoiceHandler,
    private val ttsHandler: TTSHandler
) {

    @Bean
    fun apiRouter() = router {
        ("/api" and accept(MediaType.APPLICATION_JSON)).nest {
            GET("/me", handle(userHandler::userInfo))
            GET("/guilds", handle(userHandler::mutualGuilds))
            GET("/voices", handle(voiceHandler::getVoiceLines))
            "/{guildId}".nest {
                filter(this@Routes::guildAccessFilter)
                POST("/voices/play", handle(voiceHandler::playVoiceLines))
                "/sounds".nest {
                    GET("/", handle(audioHandler::getSoundsList))
                    POST("/", handle(audioHandler::uploadSounds))
                    POST("/rnd", handle(audioHandler::playRandomSound))
                    GET("/export", handle(audioHandler::exportSounds))
                    "/{name}".nest {
                        POST("/", handle(audioHandler::updateSound))
                        DELETE("/", handleAdmin(audioHandler::deleteSound))
                        POST("/play", handle(audioHandler::playSound))
                    }
                }
                "/tts".nest {
                    POST("/play", handle(ttsHandler::playTTS))
                    POST("/random", handle(ttsHandler::playRandomTTS))
                }
            }
        }
    }

    @Bean
    fun resourceRouter() = router {
        resources {
            RouterFunctions.resourceLookupFunction("/**", ClassPathResource("static/")).andThen { res ->
                res.switchIfEmpty(Mono.just(ClassPathResource("static/index.html")))
            }.apply(it)
        }
    }

    private fun guildAccessFilter(
        req: ServerRequest,
        next: (ServerRequest) -> Mono<ServerResponse>
    ): Mono<ServerResponse> {
        val guildId = req.pathVariable("guildId")
        return requestWithPrincipal(req)
            .flatMap { (req, user) -> if (user.isInGuild(guildId)) next(req) else status(HttpStatus.FORBIDDEN).build() }
    }

    private fun handleAdmin(
        handler: (ServerRequest, DiscordUser) -> Mono<out ServerResponse>
    ): (ServerRequest) -> Mono<out ServerResponse> = { req ->
        requestWithPrincipal(req).flatMap { (req, user) ->
            val guildId = req.pathVariable("guildId")
            if (hasAdminAccess(user, guildId)) handler(req, user) else status(HttpStatus.FORBIDDEN).build()
        }
    }

    private fun handle(
        handler: (ServerRequest, DiscordUser) -> Mono<out ServerResponse>
    ): (ServerRequest) -> Mono<out ServerResponse> = { req ->
        requestWithPrincipal(req).flatMap { (req, user) -> handler(req, user) }
    }

    private fun requestWithPrincipal(req: ServerRequest): Mono<Tuple2<ServerRequest, DiscordUser>> {
        return req.principal()
            .cast(OAuth2AuthenticationToken::class.java)
            .map { Tuples.of(req, it.principal as DiscordUser) }
    }

    private fun hasAdminAccess(user: DiscordUser, guildId: String): Boolean = user.isOwner(guildId)
}
