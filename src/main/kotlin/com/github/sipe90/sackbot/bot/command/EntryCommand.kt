package com.github.sipe90.sackbot.bot.command

import com.github.sipe90.sackbot.SackException
import com.github.sipe90.sackbot.persistence.MemberRepository
import com.github.sipe90.sackbot.service.AudioFileService
import com.github.sipe90.sackbot.service.MemberService
import com.github.sipe90.sackbot.util.getGuild
import net.dv8tion.jda.api.events.Event
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

@Component
class EntryCommand(private val memberService: MemberService) : BotCommand() {

    override val commandPrefix = "entry"

    override fun process(initiator: Event, vararg command: String): Flux<String> = Flux.defer {
        val guild = getGuild(initiator)
            ?: return@defer "Could not find guild or voice channel to perform the action".toMono()

        val user = when (initiator) {
            is PrivateMessageReceivedEvent -> initiator.author
            is GuildMessageReceivedEvent -> initiator.author
            else -> throw SackException("Unsupported event type")
        }

        memberService.getMember(guild.id, user.id).flatMap { member ->
            when (command.size) {
                1 -> if (member.entrySound != null) "Your entry sound is set to `${member.entrySound}`".toMono() else
                    "Your entry sound has not yet been set".toMono()
                2 -> memberService.setMemberEntrySound(guild.id, user.id, command[1])
                        .map { "Your entry sound has been changed to `${command[1]}`" }
                else -> "Invalid entry command.".toMono()
            }
        }
    }
}