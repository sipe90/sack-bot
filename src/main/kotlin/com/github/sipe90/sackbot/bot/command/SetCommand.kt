package com.github.sipe90.sackbot.bot.command

import com.github.sipe90.sackbot.SackException
import com.github.sipe90.sackbot.service.AudioFileService
import com.github.sipe90.sackbot.service.MemberService
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class SetCommand(private val memberService: MemberService, private val audioFileService: AudioFileService) : BotCommand() {
    final override val commandName = "set"

    final override val commandData =
        Commands.slash("set", "Set your personal sounds")
            .addSubcommands(
                SubcommandData("entry", "Entry sound to be played when entering a voice channel")
                    .addOption(OptionType.STRING, "sound", "Sound name", false),
                SubcommandData("exit", "Exit sound to be played when leaving a voice channel")
                    .addOption(OptionType.STRING, "sound", "Sound name", false),
            )

    override fun process(
        initiator: SlashCommandInteractionEvent,
        guild: Guild?,
        voiceChannel: VoiceChannel?,
    ): Mono<Unit> {
        if (guild == null) {
            return sendMessage(initiator, "Cannot determine guild. You may need to join a voice channel first.")
        }

        val soundName = initiator.getOption("sound")?.asString

        return memberService.getMember(guild.id, initiator.user.id).flatMap { member ->
            when (initiator.subcommandName) {
                "entry" -> {
                    if (soundName != null) {
                        audioFileService.audioFileExists(guild.id, soundName).flatMap { exists ->
                            if (exists) {
                                memberService.setMemberEntrySound(guild.id, initiator.user.id, soundName)
                                    .thenReturn("Your entry sound has been changed to `$soundName`")
                            } else {
                                Mono.just("Could not find sound with given name")
                            }
                        }
                    } else {
                        if (member.entrySound != null) {
                            Mono.just("Your entry sound is `${member.entrySound}`")
                        } else {
                            Mono.just("Your entry sound has not yet been set")
                        }
                    }
                }
                "exit" -> {
                    if (soundName != null) {
                        audioFileService.audioFileExists(guild.id, soundName).flatMap { exists ->
                            if (exists) {
                                memberService.setMemberExitSound(guild.id, initiator.user.id, soundName)
                                    .thenReturn("Your exit sound has been changed to `$soundName`")
                            } else {
                                Mono.just("Could not find sound with given name")
                            }
                        }
                    } else {
                        if (member.exitSound != null) {
                            Mono.just("Your exit sound is `${member.exitSound}`")
                        } else {
                            Mono.just("Your exit sound has not yet been set")
                        }
                    }
                }
                else -> Mono.error(SackException("Invalid set subcommand: ${initiator.subcommandName}"))
            }
        }.flatMap { sendMessage(initiator, it) }
    }
}
