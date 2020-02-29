package com.github.sipe90.sackbot

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableAsync

@SpringBootApplication
@EnableAsync
@ConfigurationPropertiesScan("com.github.sipe90.sackbot.config")
class SackBotApplication

fun main(args: Array<String>) {
	runApplication<SackBotApplication>(*args)
}
