package com.github.sipe90.sackbot

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class SackBotApplication

fun main(args: Array<String>) {
	runApplication<SackBotApplication>(*args)
}
