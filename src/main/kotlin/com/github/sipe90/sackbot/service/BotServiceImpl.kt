package com.github.sipe90.sackbot.service

import club.minnced.jda.reactor.ReactiveEventManager
import club.minnced.jda.reactor.on
import com.github.sipe90.sackbot.config.BotConfig
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.entities.ChannelType
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import org.springframework.stereotype.Service
import javax.annotation.PostConstruct

@Service
class BotServiceImpl(val config: BotConfig) {

    private val cmdSplitRegex = Regex("\\s+")
    private val eventManager: ReactiveEventManager = ReactiveEventManager()
    private lateinit var jda: JDA

    @PostConstruct
    fun initBot() {

        eventManager.on<MessageReceivedEvent>().subscribe(this::onMessageReceived)

        jda = JDABuilder(config.token).build()
    }

    fun onMessageReceived(event: MessageReceivedEvent) {
        if (event.author.isBot) return
        if (!config.chat.allowDm && event.isFromType(ChannelType.PRIVATE)) return

        event.author.openPrivateChannel().map {
            val msg = event.message.contentRaw

            if (!msg.startsWith(config.chat.commandPrefix)) {
                return@map helpCommand(event)
            }

            val cmdStr = msg.substring(1)
            val cmd = cmdSplitRegex.split(cmdStr)

            return@map when (cmd[0]) {
                "help"   -> helpCommand(event)
                "info"   -> infoCommand(cmd, event)
                "list"   -> listCommand(cmd, event)
                "volume" -> volumeCommand(cmd, event)
                else     -> playFileCommand(cmd, event)
            }
        }
    }

    private fun helpCommand(event: MessageReceivedEvent) {
        TODO("not implemented")
    }

    private fun volumeCommand(cmd: List<String>, event: MessageReceivedEvent) {
        TODO("not implemented")
    }

    private fun playFileCommand(cmd: List<String>, event: MessageReceivedEvent) {
        TODO("not implemented")
    }

    private fun listCommand(cmd: List<String>, event: MessageReceivedEvent) {
        TODO("not implemented")
    }

    private fun infoCommand(cmd: List<String>, event: MessageReceivedEvent) {
        TODO("not implemented")
    }


}

