package com.github.sipe90.sackbot.bot.event

import com.github.sipe90.sackbot.bot.command.BotCommand
import com.github.sipe90.sackbot.service.JDAService
import com.github.sipe90.sackbot.util.getGuild
import com.github.sipe90.sackbot.util.getVoiceChannel
import jakarta.annotation.PostConstruct
import mu.KotlinLogging
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class SlashCommandEventHandler(
    private val commands: List<BotCommand>,
    private val jdaService: JDAService,
) :
    EventHandler<SlashCommandInteractionEvent> {

    private val logger = KotlinLogging.logger {}

    private val commandsMap = commands.associateBy(BotCommand::commandName)

    @PostConstruct
    private fun init() {
        jdaService.registerCommands(commands.map { it.commandData })
    }

    override fun handleEvent(event: SlashCommandInteractionEvent): Mono<Unit> {
        val command = commandsMap[event.name]
        if (command === null) {
            logger.error { "Unknown command: ${event.name}" }
            return Mono.empty()
        }
        return command.processCommand(event, getGuild(event.user), getVoiceChannel(event.user))
    }
}
