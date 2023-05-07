package com.github.sipe90.sackbot.bot.event

import club.minnced.jda.reactor.on
import com.github.sipe90.sackbot.service.JDAService
import com.github.sipe90.sackbot.util.createContext
import com.github.sipe90.sackbot.util.getMember
import net.dv8tion.jda.api.entities.channel.ChannelType
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import org.springframework.stereotype.Component
import java.util.logging.Level

@Component
class EventRegistry(
    jdaService: JDAService,
    messageEventHandler: MessageEventHandler,
    voiceChannelEventHandler: VoiceChannelEventHandler,
    slashCommandEventHandler: SlashCommandEventHandler,
) {

    private final val streamLogCategoryPrefix = "com.github.sipe90.sackbot"

    init {
        jdaService.eventManager.on<MessageReceivedEvent>()
            .log("$streamLogCategoryPrefix.MessageReceivedEvent.", Level.FINE)
            .filter { it.channelType == ChannelType.PRIVATE }
            .flatMap { event ->
                messageEventHandler.handleEvent(event)
                    .contextWrite(createContext(event.author, getMember(event.author)))
            }
            .subscribe()

        jdaService.eventManager.on<GuildVoiceUpdateEvent>()
            .log("$streamLogCategoryPrefix.GuildVoiceUpdateEvent.", Level.FINE)
            .flatMap { event ->
                voiceChannelEventHandler.handleEvent(event)
                    .contextWrite(createContext(event.member.user, event.member))
            }
            .subscribe()

        jdaService.eventManager.on<SlashCommandInteractionEvent>()
            .log("$streamLogCategoryPrefix.SlashCommandInteractionEvent.", Level.FINE)
            .flatMap { event ->
                slashCommandEventHandler.handleEvent(event)
                    .contextWrite(createContext(event.user, event.member))
            }
            .subscribe()
    }
}
