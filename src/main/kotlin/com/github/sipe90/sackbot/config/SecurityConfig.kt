package com.github.sipe90.sackbot.config

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
import org.springframework.security.oauth2.core.user.DefaultOAuth2User
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers.pathMatchers
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToFlux
import java.util.Collections

@EnableWebFluxSecurity
class SecurityConfig {

    @Bean
    fun springSecurityFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
        return http
            .authorizeExchange()
            .matchers(pathMatchers("/api/**")).authenticated()
            .anyExchange().permitAll()
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
    fun oauth2UserService(rest: WebClient): ReactiveOAuth2UserService<OAuth2UserRequest, OAuth2User> {
        val delegate = DefaultOAuth2UserService()
        return ReactiveOAuth2UserService { request: OAuth2UserRequest ->
            val user = delegate.loadUser(request)
            val client = OAuth2AuthorizedClient(request.clientRegistration, user.name, request.accessToken)

            return@ReactiveOAuth2UserService rest
                .get().uri("https://discordapp.com/api/users/@me/guilds")
                .attributes(oauth2AuthorizedClient(client))
                .retrieve()
                .bodyToFlux<Map<String, Any>>()
                .map { it["id"] }
                .collectList()
                .map {
                    val attributes = HashMap<String, Any>(user.attributes)
                    attributes["guilds"] = it

                    DefaultOAuth2User(
                        user.authorities,
                        Collections.unmodifiableMap(attributes),
                        client.clientRegistration.providerDetails.userInfoEndpoint.userNameAttributeName
                    ) as OAuth2User
                }
        }
    }
}