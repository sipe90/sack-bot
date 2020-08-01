package com.github.sipe90.sackbot.bot.command

import com.github.sipe90.sackbot.exception.WebException
import net.dv8tion.jda.api.events.Event
import org.slf4j.LoggerFactory
import reactor.core.publisher.Flux
import reactor.kotlin.core.publisher.toMono

abstract class BotCommand {

    private val logger = LoggerFactory.getLogger(javaClass)

    private val defErrorMsg = "Error happened while processing command"

    abstract val commandPrefix: String

    open fun canProcess(vararg command: String) = command.isNotEmpty() && command[0] == commandPrefix

    fun processCommand(initiator: Event, vararg command: String): Flux<String> =
            process(initiator, command)
                    .onErrorResume { throwable ->
                        logger.error(defErrorMsg, throwable)
                        (if (throwable is WebException) throwable.message ?: defErrorMsg else defErrorMsg).toMono()
                    }

    protected abstract fun process(initiator: Event, command: Array<out String>): Flux<String>

}