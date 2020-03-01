package com.github.sipe90.sackbot

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import java.util.concurrent.Executor

@SpringBootApplication
@EnableAsync
@ConfigurationPropertiesScan("com.github.sipe90.sackbot.config")
class SackBotApplication {

    @Bean
    @Primary
    fun taskExecutor(): Executor {
        val executor = ThreadPoolTaskExecutor()
        executor.initialize()
        return executor
    }
}

fun main(args: Array<String>) {
    runApplication<SackBotApplication>(*args)
}
