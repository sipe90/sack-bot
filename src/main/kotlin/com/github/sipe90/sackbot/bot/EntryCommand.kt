package com.github.sipe90.sackbot.bot

import club.minnced.jda.reactor.toMono
import com.github.sipe90.sackbot.SackException
import com.github.sipe90.sackbot.persistence.MemberRepository
import com.github.sipe90.sackbot.service.AudioFileService
import net.dv8tion.jda.api.events.Event
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent
import reactor.core.publisher.Mono

class EntryCommand(private val fileService: AudioFileService, private val memberRepository: MemberRepository) :
    AbstractBotCommand() {

    override val commandPrefix = "entry"

    override fun process(initiator: Event, vararg command: String): Mono<String> {
        val guild = getApplicableGuild(initiator)
            ?: return "Could not find guild or voice channel to perform the action".toMono()

        val user = when (initiator) {
            is PrivateMessageReceivedEvent -> initiator.author
            is GuildMessageReceivedEvent -> initiator.author
            else -> throw SackException("Unsupported event type")
        }

        return memberRepository.findOrCreate(guild.id, user.id).flatMap { member ->
            if (command.size == 1) return@flatMap if (member.entrySound != null) "Your entry sound is set to `${member.entrySound}`".toMono() else
                "Your entry sound has not yet been set".toMono()
            if (command.size == 2) return@flatMap fileService.audioFileExists(command[1]).flatMap exists@{
                if (it) {
                    member.entrySound = command[1]
                    return@exists "Your entry sound has been changed to `${member.entrySound}`".toMono()
                } else {
                    "Could not find any sounds with given name".toMono()
                }
            }
            "Invalid entry command.".toMono()
        }
    }
}