package com.github.sipe90.sackbot.bot.command

import club.minnced.jda.reactor.asMono
import com.github.sipe90.sackbot.exception.WebException
import mu.KotlinLogging
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import reactor.core.publisher.Mono

abstract class BotCommand {

    private val logger = KotlinLogging.logger {}

    private val defErrorMsg = "Error happened while processing command"

    abstract val commandName: String

    abstract val commandData: CommandData

    fun processCommand(initiator: SlashCommandInteractionEvent, guild: Guild?, voiceChannel: VoiceChannel?): Mono<Unit> {
        logger.debug { "Processing \"$commandName\" command" }
        initiator.deferReply().queue()
        return process(initiator, guild, voiceChannel)
            .onErrorResume { throwable ->
                val msg = if (throwable is WebException) throwable.message ?: defErrorMsg else defErrorMsg
                logger.error(throwable) { msg }
                sendMessage(initiator, "Something broke and it's your fault")
            }
    }

    protected abstract fun process(initiator: SlashCommandInteractionEvent, guild: Guild?, voiceChannel: VoiceChannel?): Mono<Unit>

    protected fun sendMessage(initiator: SlashCommandInteractionEvent, msg: String): Mono<Unit> {
        return initiator.hook.sendMessage(msg).asMono().then(Mono.empty())
    }
}
