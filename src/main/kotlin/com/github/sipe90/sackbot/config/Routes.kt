package com.github.sipe90.sackbot.config

import com.github.sipe90.sackbot.auth.DiscordUser
import com.github.sipe90.sackbot.handler.AudioHandler
import com.github.sipe90.sackbot.handler.UserHandler
import com.github.sipe90.sackbot.handler.VoiceHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource
import org.springframework.http.MediaType
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.web.reactive.function.server.RouterFunctions
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.router
import reactor.core.publisher.Mono

@Configuration
class Routes(
    private var userHandler: UserHandler,
    private var audioHandler: AudioHandler,
    private val voiceHandler: VoiceHandler
) {

    @Bean
    fun apiRouter() = router {
        ("/api" and accept(MediaType.APPLICATION_JSON)).nest {
            GET("/me", handle(userHandler::userInfo))
            GET("/guilds", handle(userHandler::mutualGuilds))
            GET("/voices", handle(voiceHandler::getVoiceLines))
            POST("/{guildId}/voices/play", handle(voiceHandler::playVoiceLines))
            "/{guildId}/sounds".nest {
                GET("/", handle(audioHandler::getSoundsList))
                POST("/play", handle(audioHandler::playSound))
                POST("/random", handle(audioHandler::playRandomSound))
                GET("/export", handle(audioHandler::exportSounds))
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

    private fun handle(
        handler: (ServerRequest, DiscordUser) -> Mono<out ServerResponse>
    ): (ServerRequest) -> Mono<out ServerResponse> = { req ->
        req.principal()
            .cast(OAuth2AuthenticationToken::class.java)
            .flatMap { handler(req, it.principal as DiscordUser) }
    }
}