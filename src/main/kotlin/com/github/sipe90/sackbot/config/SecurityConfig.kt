package com.github.sipe90.sackbot.config

import com.github.sipe90.sackbot.auth.DiscordGuild
import com.github.sipe90.sackbot.auth.DiscordUser
import com.github.sipe90.sackbot.persistence.MemberRepository
import com.github.sipe90.sackbot.service.JDAService
import net.dv8tion.jda.api.entities.Guild
import org.springframework.context.annotation.Bean
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest
import org.springframework.security.oauth2.client.userinfo.ReactiveOAuth2UserService
import org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction
import org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction.oauth2AuthorizedClient
import org.springframework.security.oauth2.client.web.server.ServerOAuth2AuthorizedClientRepository
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToFlux

@EnableWebFluxSecurity
class SecurityConfig {

    @Bean
    fun springSecurityFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
        return http
            .authorizeExchange()
            // .matchers(pathMatchers("/api/**")).authenticated()
            // .anyExchange().permitAll()
            .anyExchange().authenticated()
            .and().httpBasic().disable()
            .csrf().disable()
            .oauth2Client()
            .and()
            .oauth2Login()
            .and()
            .build()
    }

    @Bean
    fun rest(
        clientRegistrations: ReactiveClientRegistrationRepository,
        authorizedClients: ServerOAuth2AuthorizedClientRepository
    ): WebClient {
        return WebClient.builder()
            .filter(ServerOAuth2AuthorizedClientExchangeFilterFunction(clientRegistrations, authorizedClients)).build()
    }

    @Bean
    fun oauth2UserService(
        rest: WebClient,
        jdaService: JDAService,
        memberRepository: MemberRepository
    ): ReactiveOAuth2UserService<OAuth2UserRequest, OAuth2User> {
        val delegate = DefaultOAuth2UserService()
        return ReactiveOAuth2UserService { request ->
            val user = delegate.loadUser(request)
            val client = OAuth2AuthorizedClient(request.clientRegistration, user.name, request.accessToken)
            val attributes = DiscordUser.Attributes.getForScopes(*request.accessToken.scopes.toTypedArray())

            val userId = user.attributes[DiscordUser.Attributes.ID] as String
            val mutualGuilds = jdaService.getMutualGuilds(userId).map(Guild::getId)

            return@ReactiveOAuth2UserService rest
                .get()
                .uri("https://discordapp.com/api/users/@me/guilds")
                .attributes(oauth2AuthorizedClient(client))
                .retrieve()
                .bodyToFlux<DiscordGuild>()
                .filter { mutualGuilds.contains(it.id) }
                .flatMap { memberRepository.findOrCreate(it.id, userId).map { _ -> it } }
                .collectList()
                .map {
                    DiscordUser(
                        user.authorities,
                        user.attributes.filterKeys(attributes::contains)
                            .plus("guilds" to it)
                    )
                }
        }
    }
}