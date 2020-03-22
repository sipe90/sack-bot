package com.github.sipe90.sackbot.bot.command

import com.github.sipe90.sackbot.SackException
import com.github.sipe90.sackbot.persistence.MemberRepository
import com.github.sipe90.sackbot.service.AudioFileService
import com.github.sipe90.sackbot.util.getGuild
import net.dv8tion.jda.api.events.Event
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.kotlin.core.publisher.toMono

@Component
class ExitCommand(private val fileService: AudioFileService, private val memberRepository: MemberRepository) :
    BotCommand {

    override val commandPrefix = "exit"

    override fun process(initiator: Event, vararg command: String): Flux<String> = Flux.defer {
        val guild = getGuild(initiator)
            ?: return@defer "Could not find guild or voice channel to perform the action".toMono()

        val user = when (initiator) {
            is PrivateMessageReceivedEvent -> initiator.author
            is GuildMessageReceivedEvent -> initiator.author
            else -> throw SackException("Unsupported event type")
        }

        memberRepository.findOrCreate(guild.id, user.id).flatMap { member ->
            if (command.size == 1) return@flatMap if (member.exitSound != null) "Your exit sound is set to `${member.exitSound}`".toMono() else
                "Your exit sound has not yet been set".toMono()
            val audioName = command[1]
            if (command.size == 2) return@flatMap fileService.audioFileExists(guild.id, audioName, user.id)
                .flatMap exists@{
                    if (it) {
                        member.exitSound = audioName
                        return@exists memberRepository.updateMember(member, user.id)
                            .flatMap { "Your exit sound has been changed to `${member.entrySound}`".toMono() }
                    } else {
                        "Could not find any sounds with given name".toMono()
                    }
                }
            "Invalid exit command.".toMono()
        }
    }
}