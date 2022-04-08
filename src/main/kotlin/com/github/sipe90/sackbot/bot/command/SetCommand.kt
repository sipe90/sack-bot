package com.github.sipe90.sackbot.bot.command

import com.github.sipe90.sackbot.SackException
import com.github.sipe90.sackbot.service.MemberService
import com.github.sipe90.sackbot.util.getGuild
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

@Component
class SetCommand(private val memberService: MemberService) : BotCommand() {

    final override val commandName = "set"

    final override val commandData = Commands.slash("set", "Set your personal sounds")
        .addSubcommands(
            SubcommandData("entry", "Entry sound to be played when entering a voice channel")
                .addOption(OptionType.STRING, "sound", "Sound name", false),
            SubcommandData("exit", "Exit sound to be played when leaving a voice channel")
                .addOption(OptionType.STRING, "sound", "Sound name", false)
        )

    override fun process(initiator: SlashCommandInteractionEvent): Flux<String> = Flux.defer {
        val guild = getGuild(initiator.user)
            ?: return@defer "Could not find voice channel to perform the action".toMono()

        val soundOpt = initiator.getOption("sound")
        val soundName = soundOpt?.asString

        memberService.getMember(guild.id, initiator.user.id).flatMap { member ->
            when (initiator.subcommandName) {
                "entry" -> {
                    if (soundName != null) {
                        return@flatMap memberService.setMemberEntrySound(guild.id, initiator.user.id, soundName)
                            .then("Your entry sound has been changed to `${soundName}`".toMono())
                    }
                    return@flatMap if (member.entrySound != null) "Your entry sound is `${member.entrySound}`".toMono() else
                        "Your entry sound has not yet been set".toMono()
                }
                "exit" -> {
                    if (soundName != null) {
                        return@flatMap memberService.setMemberExitSound(guild.id, initiator.user.id, soundName)
                            .then("Your exit sound has been changed to `${soundName}`".toMono())
                    }
                    return@flatMap if (member.exitSound != null) "Your exit sound is `${member.exitSound}`".toMono() else
                        "Your exit sound has not yet been set".toMono()
                }
                else -> Mono.error(SackException("Invalid set subcommand: ${initiator.subcommandName}"))
            }
        }
    }
}