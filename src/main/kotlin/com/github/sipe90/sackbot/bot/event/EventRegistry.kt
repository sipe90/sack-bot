package com.github.sipe90.sackbot.bot.event

import club.minnced.jda.reactor.on
import com.github.sipe90.sackbot.service.JDAService
import net.dv8tion.jda.api.entities.ChannelType
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import org.springframework.stereotype.Component
import java.util.logging.Level

@Component
class EventRegistry(
    jdaService: JDAService,
    messageEventHandler: MessageEventHandler,
    voiceChannelEventHandler: VoiceChannelEventHandler,
    slashCommandEventHandler: SlashCommandEventHandler
) {

    private final val streamLogCategoryPrefix = "com.github.sipe90.sackbot"

    init {
        jdaService.eventManager.on<MessageReceivedEvent>()
            .log("$streamLogCategoryPrefix.MessageReceivedEvent.", Level.FINE)
            .filter { it.channelType == ChannelType.PRIVATE }
            .flatMap(messageEventHandler::handleEvent)
            .subscribe()

        jdaService.eventManager.on<GuildVoiceJoinEvent>()
            .log("$streamLogCategoryPrefix.GuildVoiceJoinEvent.", Level.FINE)
            .flatMap(voiceChannelEventHandler::handleEvent)
            .subscribe()

        jdaService.eventManager.on<GuildVoiceLeaveEvent>()
            .log("$streamLogCategoryPrefix.GuildVoiceLeaveEvent.", Level.FINE)
            .flatMap(voiceChannelEventHandler::handleEvent)
            .subscribe()

        jdaService.eventManager.on<SlashCommandInteractionEvent>()
            .log("$streamLogCategoryPrefix.SlashCommandInteractionEvent.", Level.FINE)
            .flatMap(slashCommandEventHandler::handleEvent)
            .subscribe()
    }
}