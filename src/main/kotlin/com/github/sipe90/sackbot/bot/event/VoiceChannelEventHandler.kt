package com.github.sipe90.sackbot.bot.event

import club.minnced.jda.reactor.toMono
import com.github.sipe90.sackbot.SackException
import com.github.sipe90.sackbot.audio.event.GuildVoiceEventEmitter
import com.github.sipe90.sackbot.service.AudioFileService
import com.github.sipe90.sackbot.service.AudioPlayerService
import com.github.sipe90.sackbot.service.MemberService
import mu.KotlinLogging
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import reactor.kotlin.core.util.function.component1
import reactor.kotlin.core.util.function.component2
import java.time.Duration

@Component
class VoiceChannelEventHandler(
    private val fileService: AudioFileService,
    private val playerService: AudioPlayerService,
    private val memberService: MemberService,
    private val voiceEventEmitter: GuildVoiceEventEmitter,
) : EventHandler<GuildVoiceUpdateEvent> {
    private val logger = KotlinLogging.logger {}

    override fun handleEvent(event: GuildVoiceUpdateEvent): Mono<Unit> {
        if (event.channelJoined != null) {
            return processGuildVoiceJoinEvent(event)
        }
        if (event.channelLeft != null) {
            return processGuildVoiceLeaveEvent(event).and(leaveEmptyVoiceChannel(event)).then(Mono.empty())
        }
        return Mono.error(SackException("Invalid event: ${event.javaClass.name}"))
    }

    private fun processGuildVoiceJoinEvent(event: GuildVoiceUpdateEvent): Mono<Unit> {
        if (event.member.user.isBot) return Mono.empty()

        val userId = event.member.user.id
        val audioChannel = event.channelJoined!!
        val guildId = event.guild.id

        return memberService.getMember(guildId, userId)
            .filter { member -> member.entrySound != null }
            .flatMap { member ->
                Mono.zip(
                    member.entrySound.toMono(),
                    fileService.audioFileExists(guildId, member.entrySound!!),
                )
            }.flatMap { (entrySound, exists) ->
                if (exists) {
                    logger.debug { "Playing user $entrySound entry sound in channel #${audioChannel.name}" }
                    playerService.playAudioInChannel(entrySound, audioChannel)
                } else {
                    logger.warn { "User ${event.member.user} has an unknown entry sound $entrySound, clearing it" }
                    memberService.setMemberEntrySound(guildId, userId, null)
                }
            }
    }

    private fun processGuildVoiceLeaveEvent(event: GuildVoiceUpdateEvent): Mono<Unit> {
        if (event.member.user.isBot) return Mono.empty()

        val userId = event.member.user.id
        val audioChannel = event.channelLeft!!
        val guildId = event.guild.id

        if (event.guild.selfMember.voiceState?.channel != audioChannel) {
            return Mono.empty()
        }

        return memberService.getMember(guildId, userId).filter { member -> member.exitSound != null }
            .flatMap { member ->
                Mono.zip(
                    member.exitSound!!.toMono(),
                    fileService.audioFileExists(guildId, member.exitSound!!),
                )
            }.flatMap { (exitSound, exists) ->
                if (exists) {
                    playerService.playAudioInChannel(exitSound, audioChannel)
                } else {
                    logger.warn { "User ${event.member.user} has an unknown exit sound $exitSound, clearing it" }
                    memberService.setMemberExitSound(guildId, userId, null)
                }
            }
    }

    private fun leaveEmptyVoiceChannel(event: GuildVoiceUpdateEvent): Mono<Unit> {
        val channel = event.channelLeft!!
        val selfMember = event.guild.selfMember
        if (channelHasMembers(channel)) {
            logger.debug { "Voice channel still has non-bot members, staying in channel" }
            return Mono.empty()
        }

        logger.debug { "No non-bot members remaining in voice channel, preparing to disconnect voice channel after 5 seconds" }

        return Mono.delay(Duration.ofSeconds(5))
            .doOnSuccess {
                if (channelHasMembers(channel) || !channel.members.contains(selfMember)) {
                    logger.debug { "Non-bot members present in voice channel, cancelling disconnect" }
                } else {
                    logger.debug { "No non-bot members still remain in voice channel, disconnecting" }
                    event.guild.audioManager.closeAudioConnection()
                    voiceEventEmitter.onVoiceChannelChange(event.guild.id, null, channel.name, null)
                }
            }.then(Mono.empty())
    }

    private fun channelHasMembers(channel: AudioChannelUnion): Boolean = channel.members.any { !it.user.isBot }
}
