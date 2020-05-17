package com.github.sipe90.sackbot.bot.command

import com.github.sipe90.sackbot.exception.WebException
import net.dv8tion.jda.api.events.Event
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import java.util.*

abstract class BotCommand {

    private val defErrorMsg = "Error happened while processing command"

    abstract val commandPrefix: String

    open fun canProcess(vararg command: String) = command.isNotEmpty() && command[0] == commandPrefix

    fun processCommand(initiator: Event, vararg command: String): Flux<String> =
        process(initiator, command)
                .onErrorResume { throwable -> (if (throwable is WebException) throwable.message ?: defErrorMsg else defErrorMsg).toMono() }

    protected abstract fun process(initiator: Event, command: Array<out String>): Flux<String>

}