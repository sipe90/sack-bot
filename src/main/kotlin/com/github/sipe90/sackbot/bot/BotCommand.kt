package com.github.sipe90.sackbot.bot

import net.dv8tion.jda.api.events.Event
import reactor.core.publisher.Mono

interface BotCommand {

    val commandPrefix: String

    fun canProcess(vararg command: String): Boolean

    fun process(initiator: Event, vararg command: String): Mono<String>
}