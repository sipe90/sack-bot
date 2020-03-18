package com.github.sipe90.sackbot.bot.command

import com.github.sipe90.sackbot.config.BotConfig
import net.dv8tion.jda.api.events.Event
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class HelpCommand(private val config: BotConfig) : BotCommand {

    override val commandPrefix = "help"

    override fun process(initiator: Event, vararg command: String): Mono<String> = Mono.fromCallable {
        StringBuilder().append("The following chat commands are available:\n")
            .append("```")
            .append(helpLine("help", "Prints this text"))
            .append(helpLine("list", "Lists all playable sound names"))
            .append(helpLine("url <url>", "Play audio from a web source"))
            .append(helpLine("random", "Play a random sound"))
            .append(helpLine("say <text>", "Text to speech"))
            .append(
                helpLine(
                    "volume <1-100>",
                    "Set sound playback volume or just /volume to get the current volume"
                )
            )
            .append(helpLine("<sound_name>", "Plays a sound with the given name"))
            .append("```\n")
            .append("You can also send me new audio files via a private message.").toString()
    }

    private fun helpLine(cmd: String, text: String): StringBuilder {
        return StringBuilder("\n")
            .append(config.chat.commandPrefix)
            .append(cmd)
            .append("                -- ".substring(cmd.length - 1))
            .append(text)
    }
}