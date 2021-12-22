package com.github.sipe90.sackbot.bot.command

import club.minnced.jda.reactor.toMono
import com.github.sipe90.sackbot.exception.WebException
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import org.slf4j.LoggerFactory
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

abstract class BotCommand {

    private val logger = LoggerFactory.getLogger(javaClass)

    private val defErrorMsg = "Error happened while processing command"

    abstract val commandName: String

    abstract val commandData: CommandData

    fun processCommand(initiator: SlashCommandEvent): Mono<Void> {
        logger.debug("Processing \"{}\" command", commandName)
        initiator.deferReply().queue()
        return process(initiator)
            .onErrorResume { throwable ->
                logger.error(defErrorMsg, throwable)
                (if (throwable is WebException) throwable.message ?: defErrorMsg else defErrorMsg).toMono()
            }.map { initiator.hook.sendMessage(it).queue() }
            .then()
    }

    protected abstract fun process(initiator: SlashCommandEvent): Flux<String>

}