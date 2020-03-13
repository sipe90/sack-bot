package com.github.sipe90.sackbot.bot.event

import club.minnced.jda.reactor.on
import com.github.sipe90.sackbot.service.JDAService
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent
import org.springframework.stereotype.Component

@Component
class EventRegistry(
    jdaService: JDAService,
    messageEventHandler: MessageEventHandler,
    voiceChannelEventHandler: VoiceChannelEventHandler
) {

    init {
        jdaService.eventManager.on<GuildMessageReceivedEvent>()
            .flatMap(messageEventHandler::handleEvent)
            .log()
            .subscribe()

        jdaService.eventManager.on<PrivateMessageReceivedEvent>()
            .flatMap(messageEventHandler::handleEvent)
            .log()
            .subscribe()

        jdaService.eventManager.on<GuildVoiceJoinEvent>()
            .flatMap(voiceChannelEventHandler::handleEvent)
            .log()
            .subscribe()

        jdaService.eventManager.on<GuildVoiceLeaveEvent>()
            .flatMap(voiceChannelEventHandler::handleEvent)
            .log()
            .subscribe()
    }
}