package com.github.sipe90.sackbot.bot.event

import com.github.sipe90.sackbot.SackException
import com.github.sipe90.sackbot.service.AudioFileService
import com.github.sipe90.sackbot.service.AudioPlayerService
import com.github.sipe90.sackbot.service.MemberService
import net.dv8tion.jda.api.events.guild.voice.GenericGuildVoiceEvent
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class VoiceChannelEventHandler(
    val fileService: AudioFileService,
    val playerService: AudioPlayerService,
    val memberService: MemberService
) : EventHandler<GenericGuildVoiceEvent> {

    private final val logger = LoggerFactory.getLogger(javaClass)

    override fun handleEvent(event: GenericGuildVoiceEvent): Mono<Void> {
        if (event is GuildVoiceJoinEvent) {
            return processGuildVoiceJoinEvent(event).then()
        }
        if (event is GuildVoiceLeaveEvent) {
            return processGuildVoiceLeaveEvent(event).then()
        }
        return Mono.error(SackException("Invalid event: ${event.javaClass.name}"))
    }

    private fun processGuildVoiceJoinEvent(event: GuildVoiceJoinEvent): Mono<Void> {
        if (event.member.user.isBot) return Mono.empty()

        val userId = event.member.user.id
        val voiceChannel = event.channelJoined
        val guildId = event.guild.id

        return memberService.getMember(guildId, userId)
            .flatMap { member ->
                val entrySound = member.entrySound ?: return@flatMap Mono.empty<Void>()
                return@flatMap fileService.audioFileExists(guildId, entrySound).flatMap exists@{ exists ->
                    if (exists) {
                        logger.debug("Playing user {} entry sound in channel #{}", entrySound, voiceChannel.name)
                        return@exists playerService.playAudioInChannel(entrySound, voiceChannel, null).then()
                    }
                    logger.warn("User {} has an unknown entry sound: {}", event.member.user, entrySound)
                    return@exists Mono.empty<Void>()
                }
            }
    }

    private fun processGuildVoiceLeaveEvent(event: GuildVoiceLeaveEvent): Mono<Void> {
        if (event.member.user.isBot) return Mono.empty()

        val userId = event.member.user.id
        val voiceChannel = event.channelLeft
        val guildId = event.guild.id

        return memberService.getMember(guildId, userId)
            .flatMap { member ->
                val exitSound = member.exitSound ?: return@flatMap Mono.empty<Void>()
                return@flatMap fileService.audioFileExists(guildId, exitSound).flatMap exists@{ exists ->
                    if (exists) {
                        return@exists playerService.playAudioInChannel(exitSound, voiceChannel, null).then()
                    }
                    logger.warn("User {} has an unknown exit sound: {}", event.member.user, exitSound)
                    return@exists Mono.empty<Void>()
                }
            }
    }
}