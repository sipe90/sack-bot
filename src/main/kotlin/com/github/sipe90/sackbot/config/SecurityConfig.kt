package com.github.sipe90.sackbot.config

import org.springframework.context.annotation.Bean
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers.pathMatchers

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
}