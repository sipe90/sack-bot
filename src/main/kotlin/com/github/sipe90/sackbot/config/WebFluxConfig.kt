package com.github.sipe90.sackbot.config

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.context.annotation.Configuration
import org.springframework.http.codec.ServerCodecConfigurer
import org.springframework.http.codec.json.Jackson2JsonDecoder
import org.springframework.http.codec.json.Jackson2JsonEncoder
import org.springframework.web.reactive.config.EnableWebFlux
import org.springframework.web.reactive.config.WebFluxConfigurer

@EnableWebFlux
@Configuration
class WebFluxConfig(private val objectMapper: ObjectMapper) : WebFluxConfigurer {

    override fun configureHttpMessageCodecs(configurer: ServerCodecConfigurer) {
        configurer.defaultCodecs().jackson2JsonEncoder(
            Jackson2JsonEncoder(objectMapper),
        )
        configurer.defaultCodecs().jackson2JsonDecoder(
            Jackson2JsonDecoder(objectMapper),
        )
    }
}
