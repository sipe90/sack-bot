package com.github.sipe90.sackbot.bot.event

import club.minnced.jda.reactor.toMono
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
import reactor.kotlin.core.util.function.component1
import reactor.kotlin.core.util.function.component2

@Component
class VoiceChannelEventHandler(
    val fileService: AudioFileService,
    val playerService: AudioPlayerService,
    val memberService: MemberService
) : EventHandler<GenericGuildVoiceEvent> {

    private final val logger = LoggerFactory.getLogger(javaClass)

    override fun handleEvent(event: GenericGuildVoiceEvent): Mono<Void> {
        if (event is GuildVoiceJoinEvent) {
            return processGuildVoiceJoinEvent(event)
        }
        if (event is GuildVoiceLeaveEvent) {
            return processGuildVoiceLeaveEvent(event)
        }
        return Mono.error(SackException("Invalid event: ${event.javaClass.name}"))
    }

    private fun processGuildVoiceJoinEvent(event: GuildVoiceJoinEvent): Mono<Void> {
        if (event.member.user.isBot) return Mono.empty()

        val userId = event.member.user.id
        val voiceChannel = event.channelJoined
        val guildId = event.guild.id

        return memberService.getMember(guildId, userId).filter { member -> member.exitSound != null }
            .flatMap { member ->
                Mono.zip(
                    member.entrySound!!.toMono(), fileService.audioFileExists(guildId, member.entrySound!!)
                )
            }.flatMap exists@{ (entrySound, exists) ->
                if (exists) {
                    logger.debug("Playing user {} entry sound in channel #{}", entrySound, voiceChannel.name)
                    return@exists playerService.playAudioInChannel(entrySound, voiceChannel, null)
                }
                logger.warn("User {} has an unknown entry sound {}, clearing it", event.member.user, entrySound)
                memberService.setMemberEntrySound(guildId, userId, null).then()
            }
    }

    private fun processGuildVoiceLeaveEvent(event: GuildVoiceLeaveEvent): Mono<Void> {
        if (event.member.user.isBot) return Mono.empty()

        val userId = event.member.user.id
        val voiceChannel = event.channelLeft
        val guildId = event.guild.id

        return memberService.getMember(guildId, userId).filter { member -> member.exitSound != null }
            .flatMap { member ->
                Mono.zip(
                    member.exitSound!!.toMono(), fileService.audioFileExists(guildId, member.exitSound!!)
                )
            }.flatMap exists@{ (exitSound, exists) ->
                if (exists) {
                    return@exists playerService.playAudioInChannel(exitSound, voiceChannel, null)
                }
                logger.warn("User {} has an unknown exit sound {}, clearing it", event.member.user, exitSound)
                memberService.setMemberExitSound(guildId, userId, null).then()
            }
    }
}