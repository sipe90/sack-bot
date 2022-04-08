package com.github.sipe90.sackbot.bot.event

import com.github.sipe90.sackbot.bot.command.BotCommand
import com.github.sipe90.sackbot.service.JDAService
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class SlashCommandEventHandler(commands: List<BotCommand>, jdaService: JDAService) :
    EventHandler<SlashCommandInteractionEvent> {

    private final val logger = LoggerFactory.getLogger(javaClass)

    private val commandsMap = commands.associateBy(BotCommand::commandName)

    init {
        jdaService.registerCommands(commands.map { it.commandData })
    }

    override fun handleEvent(event: SlashCommandInteractionEvent): Mono<Void> {
        val command = commandsMap[event.name]
        if (command === null) {
            logger.error("Unknown command: ${event.name}")
            return Mono.empty()
        }
        return command.processCommand(event)
    }
}